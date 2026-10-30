package org.bukkit.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PluginDescriptionFile {
    private final String name;
    private final String main;
    private final String version;
    private final String description;
    private final List<String> authors;
    private final List<String> depend;
    private final List<String> softDepend;
    private final List<String> loadBefore;
    private final String apiVersion;
    private final Map<String, Map<String, Object>> commands;
    private final Map<String, Map<String, Object>> permissions;

    public PluginDescriptionFile(String name, String version, String main) {
        this(name, main, version, "", List.of(), List.of(), List.of(), List.of(), null, Map.of(), Map.of());
    }

    private PluginDescriptionFile(
            String name,
            String main,
            String version,
            String description,
            List<String> authors,
            List<String> depend,
            List<String> softDepend,
            List<String> loadBefore,
            String apiVersion,
            Map<String, Map<String, Object>> commands,
            Map<String, Map<String, Object>> permissions
    ) {
        this.name = name;
        this.main = main;
        this.version = version;
        this.description = description;
        this.authors = authors;
        this.depend = depend;
        this.softDepend = softDepend;
        this.loadBefore = loadBefore;
        this.apiVersion = apiVersion;
        this.commands = commands;
        this.permissions = permissions;
    }

    @SuppressWarnings("unchecked")
    public static PluginDescriptionFile from(Map<String, Object> yaml) {
        String name = string(yaml.get("name"));
        String main = string(yaml.get("main"));
        String version = string(yaml.get("version"));
        if (name == null || main == null || version == null) {
            throw new IllegalArgumentException("plugin.yml needs name, main and version");
        }
        List<String> authors = stringList(yaml.get("authors"));
        if (authors.isEmpty() && yaml.get("author") != null) authors = List.of(string(yaml.get("author")));
        return new PluginDescriptionFile(
                name,
                main,
                version,
                string(yaml.get("description")) == null ? "" : string(yaml.get("description")),
                authors,
                stringList(yaml.get("depend")),
                stringList(first(yaml, "softdepend", "softDepend")),
                stringList(first(yaml, "loadbefore", "loadBefore")),
                string(yaml.get("api-version")),
                sectionMap(yaml.get("commands")),
                sectionMap(yaml.get("permissions"))
        );
    }

    public String getName() {
        return name;
    }

    public String getMain() {
        return main;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public List<String> getDepend() {
        return depend;
    }

    public List<String> getSoftDepend() {
        return softDepend;
    }

    public List<String> getLoadBefore() {
        return loadBefore;
    }

    public String getAPIVersion() {
        return apiVersion;
    }

    public String getFullName() {
        return name + " v" + version;
    }

    public Map<String, Map<String, Object>> getCommands() {
        return commands;
    }

    public Map<String, Map<String, Object>> getPermissions() {
        return permissions;
    }

    private static Object first(Map<String, Object> yaml, String a, String b) {
        return yaml.containsKey(a) ? yaml.get(a) : yaml.get(b);
    }

    private static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static List<String> stringList(Object value) {
        if (value == null) return List.of();
        if (value instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object item : list) if (item != null) out.add(String.valueOf(item));
            return List.copyOf(out);
        }
        return List.of(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> sectionMap(Object value) {
        if (!(value instanceof Map<?, ?> raw)) return Map.of();
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            Map<String, Object> child = new LinkedHashMap<>();
            if (entry.getValue() instanceof Map<?, ?> nested) {
                for (Map.Entry<?, ?> item : nested.entrySet()) child.put(String.valueOf(item.getKey()), item.getValue());
            }
            out.put(String.valueOf(entry.getKey()), child);
        }
        return Collections.unmodifiableMap(out);
    }
}
