package org.bukkit.plugin;

public interface PluginLoader {
    void enablePlugin(Plugin plugin);

    void disablePlugin(Plugin plugin);
}
