package org.bukkit.plugin;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.WorldGenerator;

public interface Plugin extends TabExecutor {
    File getDataFolder();

    PluginDescriptionFile getDescription();

    FileConfiguration getConfig();

    InputStream getResource(String filename);

    void saveConfig();

    void saveDefaultConfig();

    void saveResource(String resourcePath, boolean replace);

    void reloadConfig();

    PluginLoader getPluginLoader();

    Server getServer();

    boolean isEnabled();

    void onDisable();

    void onLoad();

    void onEnable();

    boolean isNaggable();

    void setNaggable(boolean canNag);

    Logger getLogger();

    String getName();

    default WorldGenerator getDefaultWorldGenerator(String worldName, String id) {
        return null;
    }
}
