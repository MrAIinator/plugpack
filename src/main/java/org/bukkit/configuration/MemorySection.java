package org.bukkit.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemorySection implements ConfigurationSection {
    private final Map<String, Object> values = new LinkedHashMap<>();
    private MemorySection defaults;
    private boolean copyDefaults;

    public void setDefaults(ConfigurationSection defaults) {
        if (defaults instanceof MemorySection section) this.defaults = section;
    }

    public void setCopyDefaults(boolean copyDefaults) {
        this.copyDefaults = copyDefaults;
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        Set<String> keys = new LinkedHashSet<>(values.keySet());
        if (deep) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (entry.getValue() instanceof MemorySection child) {
                    for (String nested : child.getKeys(true)) keys.add(entry.getKey() + "." + nested);
                }
            }
        }
        return keys;
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : getKeys(deep)) out.put(key, get(key));
        return out;
    }

    @Override
    public boolean contains(String path) {
        return get(path, null) != null || (defaults != null && defaults.get(path, null) != null);
    }

    @Override
    public Object get(String path) {
        return get(path, null);
    }

    @Override
    public Object get(String path, Object def) {
        Object value = lookup(path);
        if (value == null && defaults != null) {
            Object fromDefault = defaults.lookup(path);
            if (fromDefault != null) {
                if (copyDefaults) set(path, copy(fromDefault));
                return copyDefaults ? lookup(path) : fromDefault;
            }
        }
        return value == null ? def : value;
    }

    private Object lookup(String path) {
        if (path == null || path.isEmpty()) return null;
        int dot = path.indexOf('.');
        if (dot < 0) return values.get(path);
        Object child = values.get(path.substring(0, dot));
        if (child instanceof MemorySection section) return section.lookup(path.substring(dot + 1));
        return null;
    }

    @Override
    public void set(String path, Object value) {
        if (path == null || path.isEmpty()) return;
        int dot = path.indexOf('.');
        if (dot < 0) {
            if (value == null) values.remove(path);
            else values.put(path, normalize(value));
            return;
        }
        String head = path.substring(0, dot);
        Object child = values.get(head);
        MemorySection section;
        if (child instanceof MemorySection existing) section = existing;
        else {
            section = new MemorySection();
            values.put(head, section);
        }
        section.set(path.substring(dot + 1), value);
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public String getString(String path, String def) {
        Object value = get(path, def);
        return value == null ? def : String.valueOf(value);
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int def) {
        Object value = get(path, def);
        if (value instanceof Number number) return number.intValue();
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return def;
            }
        }
        return def;
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Object value = get(path, def);
        if (value instanceof Boolean bool) return bool;
        if (value instanceof String text) return Boolean.parseBoolean(text);
        return def;
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0);
    }

    @Override
    public double getDouble(String path, double def) {
        Object value = get(path, def);
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException ignored) {
                return def;
            }
        }
        return def;
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0L);
    }

    @Override
    public long getLong(String path, long def) {
        Object value = get(path, def);
        if (value instanceof Number number) return number.longValue();
        if (value instanceof String text) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return def;
            }
        }
        return def;
    }

    @Override
    public List<String> getStringList(String path) {
        List<?> raw = getList(path);
        List<String> out = new ArrayList<>();
        if (raw == null) return out;
        for (Object item : raw) if (item != null) out.add(String.valueOf(item));
        return out;
    }

    @Override
    public List<?> getList(String path) {
        Object value = get(path);
        if (value instanceof List<?> list) return list;
        return null;
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        Object value = get(path);
        return value instanceof ConfigurationSection section ? section : null;
    }

    @Override
    public ConfigurationSection createSection(String path) {
        MemorySection section = new MemorySection();
        set(path, section);
        Object stored = lookup(path);
        return stored instanceof ConfigurationSection created ? created : section;
    }

    @Override
    public void addDefault(String path, Object value) {
        if (defaults == null) defaults = new MemorySection();
        defaults.set(path, value);
    }

    public Map<String, Object> export() {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) out.put(entry.getKey(), exportValue(entry.getValue()));
        return out;
    }

    private static Object exportValue(Object value) {
        if (value instanceof MemorySection section) return section.export();
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            for (Object item : list) copy.add(exportValue(item));
            return copy;
        }
        return value;
    }

    private static Object normalize(Object value) {
        if (value instanceof ConfigurationSection section && section instanceof MemorySection memory) return memory;
        if (value instanceof Map<?, ?> map) {
            MemorySection section = new MemorySection();
            for (Map.Entry<?, ?> entry : map.entrySet()) section.values.put(String.valueOf(entry.getKey()), normalize(entry.getValue()));
            return section;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            for (Object item : list) copy.add(normalize(item));
            return copy;
        }
        return value;
    }

    private static Object copy(Object value) {
        return normalize(exportValue(value));
    }
}
