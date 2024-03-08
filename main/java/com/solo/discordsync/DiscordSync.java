package com.solo.discordsync;

import com.solo.discordsync.commands.linkCommand;
import com.solo.discordsync.discord.DiscordBot;
import com.solo.discordsync.discord.Enviroment;
import com.solo.discordsync.hooks.LuckPermsHook;
import com.solo.discordsync.listeners.PlayerJoinQuit;
import com.solo.discordsync.util.Database;
import net.dv8tion.jda.api.JDA;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class DiscordSync extends JavaPlugin {

    private static DiscordSync instance;
    private LuckPermsHook luckpermsHook;
    private Enviroment enviroment;
    private JDA jda;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        new Enviroment(this);
        new DiscordBot(this);

        try {
            Database.onEnable(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> rankToRoleId = loadRolesFromConfig();

        luckpermsHook = new LuckPermsHook();
        getServer().getPluginManager().registerEvents(new PlayerJoinQuit(luckpermsHook.getAPI()), this);

        getCommand("link").setExecutor(new linkCommand(jda, rankToRoleId));

        getLogger().info("DiscordSync is ready.");
    }

    @Override
    public void onDisable() {
        try {
            DiscordBot.shutdown();
        } catch (Exception ignore) {
        }
        try {
            Database.onDisable();
        } catch (Exception ignore) {
        }
    }

    public static DiscordSync getInstance() {
        return instance;
    }

    public Map<String, String> loadRolesFromConfig() {
        Map<String, String> rankToRoleId = new HashMap<>();
        ConfigurationSection rolesSection = getConfig().getConfigurationSection("discord.roles");
        if (rolesSection != null) {
            for (String key : rolesSection.getKeys(false)) {
                String value = rolesSection.getString(key);
                if (value != null) {
                    rankToRoleId.put(key, value);
                } else {
                    getLogger().info("Value for key " + key + " is not a string.");
                }
            }
        } else {
            getLogger().info("discord.roles section is not found in the config file.");
        }
        getLogger().info("Loaded roles: " + rankToRoleId);
        return rankToRoleId;
    }
}
