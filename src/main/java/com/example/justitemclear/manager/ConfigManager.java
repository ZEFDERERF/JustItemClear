package com.example.justitemclear.manager;

import com.example.justitemclear.justitemclear;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import java.io.File;

public class ConfigManager {

    private static ConfigManager instance;
    private final justitemclear plugin;
    private FileConfiguration config;

    private int itemCleanupInterval;
    private boolean cleanupItemsEnabled;
    private List<String> worldWhitelist;
    private int countdownDuration;
    
    private ConfigManager(justitemclear plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public static void initialize(justitemclear plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
            instance.loadConfigValues();
        }
    }
    
    public static ConfigManager getInstance() {
        return instance;
    }

    public void loadConfigValues() {

        int configMinutes = config.getInt("cleanup-interval.items", 10);
        this.itemCleanupInterval = configMinutes * 60;

        this.cleanupItemsEnabled = config.getBoolean("cleanup.enabled.items", true);
        this.countdownDuration = 60; 
        this.worldWhitelist = config.getStringList("cleanup.world-whitelist");
    }
    
    public void reloadConfig() {
        if (!plugin.getDataFolder().exists() || !new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveDefaultConfig();
        }
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadConfigValues();
    }
    
    public int getItemCleanupInterval() {
        return itemCleanupInterval;
    }
    
    public boolean isCleanupItemsEnabled() {
        return cleanupItemsEnabled;
    }
    
    public List<String> getWorldWhitelist() {
        return worldWhitelist;
    }
    
    public int getCountdownDuration() {
        return countdownDuration;
    }
    
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
}