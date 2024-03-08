package com.solo.discordsync.discord;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class Enviroment {
    private static FileConfiguration config;
    private final File configFile;

    public Enviroment(Plugin plugin) {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static String get(String path) {
        return config.getString(path);
    }

    public FileConfiguration getAllConfigs() {
        return config;
    }
}
