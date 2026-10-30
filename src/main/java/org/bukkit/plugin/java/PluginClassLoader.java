package org.bukkit.plugin.java;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public final class PluginClassLoader extends URLClassLoader {
    private final PluginDescriptionFile description;
    private final File jarFile;
    private final File dataFolder;
    private JavaPlugin plugin;

    public PluginClassLoader(URL jarUrl, File jarFile, File dataFolder, PluginDescriptionFile description, ClassLoader parent) {
        super(new URL[]{jarUrl}, parent);
        this.jarFile = jarFile;
        this.dataFolder = dataFolder;
        this.description = description;
    }

    public void initialize(JavaPlugin plugin) {
        if (this.plugin != null && this.plugin != plugin) {
            throw new IllegalStateException("Plugin already initialized");
        }
        this.plugin = plugin;
        plugin.init(this, description, dataFolder, jarFile, logger(description.getName()));
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                if (parentFirst(name)) {
                    loaded = getParent().loadClass(name);
                } else {
                    try {
                        loaded = findClass(name);
                    } catch (ClassNotFoundException ignored) {
                        loaded = getParent().loadClass(name);
                    }
                }
            }
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }

    private static boolean parentFirst(String name) {
        return name.startsWith("java.")
                || name.startsWith("javax.")
                || name.startsWith("jdk.")
                || name.startsWith("sun.")
                || name.startsWith("org.bukkit.")
                || name.startsWith("net.minecraft.")
                || name.startsWith("net.neoforged.")
                || name.startsWith("com.mojang.")
                || name.startsWith("net.md_5.bungee.")
                || name.startsWith("net.milkbowl.vault.")
                || name.startsWith("org.slf4j.")
                || name.startsWith("org.apache.logging.");
    }

    private static Logger logger(String name) {
        Logger logger = Logger.getLogger("PlugPack/" + name);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        if (logger.getHandlers().length == 0) {
            logger.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    if (record == null) return;
                    String message = "[" + name + "] " + record.getMessage();
                    if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
                        dev.mrainator.plugpack.PlugPack.LOGGER.error(message, record.getThrown());
                    } else if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                        dev.mrainator.plugpack.PlugPack.LOGGER.warn(message, record.getThrown());
                    } else {
                        dev.mrainator.plugpack.PlugPack.LOGGER.info(message, record.getThrown());
                    }
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() {
                }
            });
        }
        return logger;
    }
}
