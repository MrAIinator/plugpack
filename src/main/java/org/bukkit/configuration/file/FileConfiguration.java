package org.bukkit.configuration.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.bukkit.configuration.MemorySection;

public class FileConfiguration extends MemorySection {
    public void save(File file) throws IOException {
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), saveToString());
    }

    public String saveToString() {
        return dev.mrainator.plugpack.internal.YamlParser.emit(export());
    }

    public void load(File file) throws IOException {
        if (file == null || !file.isFile()) return;
        loadFromString(Files.readString(file.toPath()));
    }

    public void loadFromString(String contents) {
        MapCopy.loadInto(this, dev.mrainator.plugpack.internal.YamlParser.parse(contents));
    }

    /** Clears current keys then copies parsed values. */
    static final class MapCopy {
        private MapCopy() {
        }

        static void loadInto(FileConfiguration target, java.util.Map<String, Object> map) {
            for (String key : new java.util.ArrayList<>(target.getKeys(false))) target.set(key, null);
            for (java.util.Map.Entry<String, Object> entry : map.entrySet()) target.set(entry.getKey(), entry.getValue());
        }
    }
}
