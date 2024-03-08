package com.solo.discordsync.util;

import com.solo.discordsync.DiscordSync;
import com.solo.discordsync.discord.DiscordBot;
import com.solo.discordsync.discord.Server;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static org.bukkit.Bukkit.getLogger;


public class Database {
    private static String url;
    private static String user;
    private static String psw;
    private static Connection connection;

    private static final long IDLE_TIMEOUT = 1800000;
    private static long lastConnectionTime = System.currentTimeMillis();
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final ReentrantLock lock = new ReentrantLock();

    public static synchronized Connection getConnection() throws SQLException {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            establishConnection();
            lastConnectionTime = currentTime;
            return connection;
        } finally {
            lock.unlock();
        }
    }

    private static synchronized void establishConnection() throws SQLException {
        lock.lock();
        try {
            connection = DriverManager.getConnection(url, user, psw);
        } finally {
            lock.unlock();
        }
    }

    private static synchronized void destroyConnection() throws SQLException {
        lock.lock();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } finally {
            lock.unlock();
        }
    }

    public static String getMinecraftUUIDByMinecraftName(String minecraftName) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM mcProfiles WHERE username = ?")) {
            statement.setString(1, minecraftName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("uuid");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String getDiscordIdByMinecraftUUID(String minecraftUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT discord_id FROM mcProfiles WHERE uuid = ?")) {
            statement.setString(1, minecraftUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("discord_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String getRankByMinecraftUUID(String minecraftUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT rank FROM mcProfiles WHERE uuid = ?")) {
            statement.setString(1, minecraftUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("rank");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public static String getDiscordNameByMinecraftUUID(String minecraftUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT discord_name FROM dcProfiles WHERE discord_id = (SELECT discord_id FROM mcProfiles WHERE uuid = ?)")) {
            statement.setString(1, minecraftUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("discord_name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public static int getOnlineStatusByMinecraftUUID(String minecraftUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT online_status FROM mcProfiles WHERE uuid = ?")) {
            statement.setString(1, minecraftUUID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("online_status");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public static String getRankByUUID(String uuid) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT rank FROM mcProfiles WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("rank");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public static boolean isValidSyncCode(String syncCode) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM dcProfiles WHERE sync_code = ?")) {
            statement.setString(1, syncCode);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDiscordIdBySyncCode(String syncCode) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT discord_id FROM dcProfiles WHERE sync_code = ?")) {
            statement.setString(1, syncCode);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("discord_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }



    public static boolean isAccountLinked(String syncCode) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT synced FROM dcProfiles WHERE sync_code = ?")) {
            statement.setString(1, syncCode);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("synced") == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static void linkMinecraftToDiscord(String minecraftUUID, String discordUserId) {
        Bukkit.getScheduler().runTaskAsynchronously(DiscordSync.getInstance(), () -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE mcProfiles SET discord_id = ? WHERE uuid = ?")) {
                    statement.setString(1, discordUserId);
                    statement.setString(2, minecraftUUID);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement("UPDATE dcProfiles SET synced = 1 WHERE discord_id = ?")) {
                    statement.setString(1, discordUserId);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                handleSQLException(e, "Error linking Minecraft account to Discord account.");
            }
        });
    }

    public static void updateStatus(String uuid, int status) {
        Bukkit.getScheduler().runTaskAsynchronously(DiscordSync.getInstance(), () -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE mcProfiles SET online_status = ? WHERE uuid = ?")) {
                statement.setInt(1, status);
                statement.setString(2, uuid);
                statement.executeUpdate();
            } catch (SQLException e) {
                handleSQLException(e, "Error updating online status in the database. UUID: " + uuid);
            }
        });
    }

    public static boolean findById(String uuid) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM mcProfiles WHERE uuid = ?")) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSyncCodeByDiscordId(String discordUserId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT sync_code FROM dcProfiles WHERE discord_id = ?")) {
            statement.setString(1, discordUserId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("sync_code");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static boolean findDiscordUserById(String discordUserId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM dcProfiles WHERE discord_id = ?")) {
            statement.setString(1, discordUserId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<Boolean> createDiscordUserAsync(String discordUserId, String discordUserName) {
        return CompletableFuture.supplyAsync(() -> createDiscordUser(discordUserId, discordUserName), executor);
    }

    private static boolean createDiscordUser(String discordUserId, String discordUserName) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO dcProfiles (discord_id , discord_name, sync_code) VALUES (?, ?, ?)")) {
            statement.setString(1, discordUserId);
            statement.setString(2, discordUserName);
            String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            Random random = new Random();
            StringBuilder syncCode = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                syncCode.append(symbols.charAt(random.nextInt(symbols.length())));
            }
            statement.setString(3, syncCode.toString());
            statement.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("User already exists in the database. DiscordUserID: " + discordUserId);
            return false;
        } catch (SQLException e) {
            handleSQLException(e, "Error inserting user data into the database. DiscordUserID: " + discordUserId);
            return false;
        }
    }

    public static void createMcProfileAsync(String uuid, String name, String rank) {
        Bukkit.getScheduler().runTaskAsynchronously(DiscordSync.getInstance(), () -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO mcProfiles (uuid, username, rank, online_status) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, uuid);
                statement.setString(2, name);
                statement.setString(3, rank);
                statement.setInt(4, 1); // Set online_status to 1
                statement.executeUpdate();
                getLogger().info("Player " + name + " registered successfully.");
            } catch (SQLException e) {
                handleSQLException(e, "Error inserting player data into the database. UUID: " + uuid + ", Name: " + name);
            }
        });
    }
    public static void onEnable(Plugin p) throws SQLException {
        String ip = p.getConfig().getString("database.ip");
        String name = p.getConfig().getString("database.name");
        url = "jdbc:mysql://" + ip + "/" + name;
        user = p.getConfig().getString("database.user");
        psw = p.getConfig().getString("database.password");
        createTables();
        executorService.scheduleWithFixedDelay(() -> {
            long currentTime = System.currentTimeMillis();
            if (connection != null && (currentTime - lastConnectionTime) > IDLE_TIMEOUT) {
                try {
                    destroyConnection();
                    establishConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, IDLE_TIMEOUT, IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static void onDisable() throws SQLException {
        destroyConnection();
        executorService.shutdown();
    }

    private static void createTables() throws SQLException {
        Connection connection = getConnection();
        Statement statementTitleTable = connection.createStatement();
        String mcProfiles = "CREATE TABLE IF NOT EXISTS mcProfiles (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "uuid VARCHAR(36) UNIQUE NOT NULL," +
                "username VARCHAR(16) NOT NULL," +
                "rank VARCHAR(16) NOT NULL," +
                "online_status TINYINT(1) DEFAULT 0," +
                "discord_id VARCHAR(255)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (discord_id) REFERENCES dcProfiles(discord_id)" +
                ")";


        String dcProfiles = "CREATE TABLE IF NOT EXISTS dcProfiles (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "discord_id VARCHAR(255) UNIQUE NOT NULL," +
                "discord_name VARCHAR(255) NOT NULL, " +
                "sync_code VARCHAR(10) NOT NULL, " +
                "synced TINYINT(1) DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
        statementTitleTable.execute(dcProfiles);
        statementTitleTable.execute(mcProfiles);

        statementTitleTable.close();
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[DB] Tables loaded");
    }

    private static void handleSQLException(SQLException e, String errorMessage) {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[DB] " + errorMessage + " " + e.getMessage());
    }
}
