package org.bukkit.configuration.file;

import java.io.File;
import java.io.IOException;
import dev.mrainator.plugpack.PlugPack;

public class YamlConfiguration extends FileConfiguration {
    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration configuration = new YamlConfiguration();
        if (file != null && file.isFile()) {
            try {
                configuration.load(file);
            } catch (IOException | RuntimeException exception) {
                PlugPack.LOGGER.error("Could not load {}", file, exception);
            }
        }
        return configuration;
    }
}
