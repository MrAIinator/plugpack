package org.bukkit.plugin.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

public abstract class JavaPlugin extends PluginBase {
    private boolean enabled;
    private PluginDescriptionFile description;
    private File dataFolder;
    private File file;
    private Logger logger;
    private PluginClassLoader classLoader;
    private File configFile;
    private FileConfiguration newConfig;
    private boolean naggable = true;

    public JavaPlugin() {
        ClassLoader loader = getClass().getClassLoader();
        if (!(loader instanceof PluginClassLoader pluginLoader)) {
            throw new IllegalStateException("JavaPlugin requires " + PluginClassLoader.class.getName());
        }
        pluginLoader.initialize(this);
    }

    final void init(PluginClassLoader classLoader, PluginDescriptionFile description, File dataFolder, File file, Logger logger) {
        this.classLoader = classLoader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        this.logger = logger;
        this.configFile = new File(dataFolder, "config.yml");
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return description;
    }

    @Override
    public FileConfiguration getConfig() {
        if (newConfig == null) reloadConfig();
        return newConfig;
    }

    @Override
    public void reloadConfig() {
        newConfig = YamlConfiguration.loadConfiguration(configFile);
        InputStream def = getResource("config.yml");
        if (def != null) {
            try (def) {
                YamlConfiguration defaults = new YamlConfiguration();
                defaults.loadFromString(new String(def.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                newConfig.setDefaults(defaults);
                newConfig.setCopyDefaults(true);
            } catch (IOException exception) {
                logger.log(Level.WARNING, "Could not load config defaults", exception);
            }
        }
    }

    @Override
    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Could not save config to " + configFile, exception);
        }
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) saveResource("config.yml", false);
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) throw new IllegalArgumentException("Resource path empty");
        File out = new File(dataFolder, resourcePath);
        if (out.exists() && !replace) return;
        try (InputStream in = getResource(resourcePath)) {
            if (in == null) throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
            File parent = out.getParentFile();
            if (parent != null) parent.mkdirs();
            Files.copy(in, out.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException("Could not save " + resourcePath, exception);
        }
    }

    @Override
    public InputStream getResource(String filename) {
        if (filename == null) return null;
        return classLoader.getResourceAsStream(filename.startsWith("/") ? filename.substring(1) : filename);
    }

    @Override
    public PluginLoader getPluginLoader() {
        return Bukkit.getPluginManager() instanceof PluginLoader loader ? loader : null;
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        if (enabled) {
            this.enabled = true;
            try {
                onEnable();
            } catch (Throwable thrown) {
                this.enabled = false;
                logger.log(Level.SEVERE, "Error occurred while enabling " + getName(), thrown);
            }
        } else {
            try {
                onDisable();
            } catch (Throwable thrown) {
                logger.log(Level.SEVERE, "Error occurred while disabling " + getName(), thrown);
            } finally {
                this.enabled = false;
            }
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public boolean isNaggable() {
        return naggable;
    }

    @Override
    public void setNaggable(boolean canNag) {
        this.naggable = canNag;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getName() {
        return description.getName();
    }

    public File getFile() {
        return file;
    }

    public PluginCommand getCommand(String name) {
        String needle = name.toLowerCase(java.util.Locale.ROOT);
        PluginCommand command = Bukkit.getServer().getPluginCommand(needle);
        if (command != null && command.getPlugin() == this) return command;
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    public static JavaPlugin getPlugin(Class<?> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        if (loader instanceof PluginClassLoader pluginLoader && pluginLoader.getPlugin() instanceof JavaPlugin plugin) {
            return plugin;
        }
        throw new IllegalArgumentException(clazz + " is not a PlugPack plugin class");
    }

    public static JavaPlugin getProvidingPlugin(Class<?> clazz) {
        return getPlugin(clazz);
    }
}
