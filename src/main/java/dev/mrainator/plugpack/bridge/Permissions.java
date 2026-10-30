package dev.mrainator.plugpack.bridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.plugin.PluginDescriptionFile;

public final class Permissions {
    public enum DefaultMode {TRUE, FALSE, OP, NOT_OP}

    private final Path file;
    private final boolean opHasAll;
    private final Map<String, DefaultMode> defaults = new HashMap<>();
    private final Map<UUID, Map<String, Boolean>> explicit = new HashMap<>();

    public Permissions(Path file, boolean opHasAll) {
        this.file = file;
        this.opHasAll = opHasAll;
        load();
    }

    public void addDefaults(PluginDescriptionFile description) {
        for (Map.Entry<String, Map<String, Object>> entry : description.getPermissions().entrySet()) {
            Object raw = entry.getValue().get("default");
            defaults.put(entry.getKey().toLowerCase(Locale.ROOT), parse(raw));
        }
    }

    public boolean isSet(UUID id, String node, boolean op) {
        return explicitValue(id, node) != null;
    }

    public boolean has(UUID id, String node, boolean op) {
        if (node == null || node.isEmpty()) return true;
        Boolean exact = explicitValue(id, node);
        if (exact != null) return exact;
        if (opHasAll && op) return true;
        DefaultMode mode = defaults.get(node.toLowerCase(Locale.ROOT));
        if (mode == null) return op;
        return switch (mode) {
            case TRUE -> true;
            case FALSE -> false;
            case OP -> op;
            case NOT_OP -> !op;
        };
    }

    public void set(UUID id, String node, boolean value) {
        explicit.computeIfAbsent(id, ignored -> new HashMap<>()).put(node.toLowerCase(Locale.ROOT), value);
        save();
    }

    private Boolean explicitValue(UUID id, String node) {
        Map<String, Boolean> nodes = explicit.get(id);
        if (nodes == null || node == null) return null;
        String key = node.toLowerCase(Locale.ROOT);
        if (nodes.containsKey(key)) return nodes.get(key);
        String cursor = key;
        while (cursor.contains(".")) {
            cursor = cursor.substring(0, cursor.lastIndexOf('.'));
            if (nodes.containsKey(cursor + ".*")) return nodes.get(cursor + ".*");
        }
        if (nodes.containsKey("*")) return nodes.get("*");
        return null;
    }

    private static DefaultMode parse(Object raw) {
        if (raw == null) return DefaultMode.OP;
        if (raw instanceof Boolean bool) return bool ? DefaultMode.TRUE : DefaultMode.FALSE;
        String text = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
        return switch (text) {
            case "true" -> DefaultMode.TRUE;
            case "false" -> DefaultMode.FALSE;
            case "not op", "notop" -> DefaultMode.NOT_OP;
            default -> DefaultMode.OP;
        };
    }

    private void load() {
        if (!Files.isRegularFile(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int eq = trimmed.indexOf('=');
                int dot = trimmed.indexOf('.');
                if (eq < 0 || dot < 0 || dot > eq) continue;
                UUID id = UUID.fromString(trimmed.substring(0, dot));
                String node = trimmed.substring(dot + 1, eq).toLowerCase(Locale.ROOT);
                boolean value = Boolean.parseBoolean(trimmed.substring(eq + 1).trim());
                explicit.computeIfAbsent(id, ignored -> new HashMap<>()).put(node, value);
            }
        } catch (Exception exception) {
            dev.mrainator.plugpack.PlugPack.LOGGER.error("Could not read {}", file, exception);
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            StringBuilder out = new StringBuilder("# uuid.node=true\n");
            for (Map.Entry<UUID, Map<String, Boolean>> player : explicit.entrySet()) {
                for (Map.Entry<String, Boolean> node : player.getValue().entrySet()) {
                    out.append(player.getKey()).append('.').append(node.getKey()).append('=').append(node.getValue()).append('\n');
                }
            }
            Files.writeString(file, out.toString());
        } catch (IOException exception) {
            dev.mrainator.plugpack.PlugPack.LOGGER.error("Could not save {}", file, exception);
        }
    }
}
