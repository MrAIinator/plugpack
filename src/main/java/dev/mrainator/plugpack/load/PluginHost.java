package dev.mrainator.plugpack.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import dev.mrainator.plugpack.PlugPack;
import dev.mrainator.plugpack.bridge.NeoServer;
import dev.mrainator.plugpack.internal.YamlParser;
import dev.mrainator.plugpack.vault.VaultBridge;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

public final class PluginHost {
    private final NeoServer server;
    private final List<Loaded> loaded = new ArrayList<>();
    private Path folder;

    public PluginHost(NeoServer server) {
        this.server = server;
    }

    public int start(Path folder) {
        this.folder = folder;
        try {
            Files.createDirectories(folder);
            Path readme = folder.resolve("README.txt");
            if (!Files.exists(readme)) {
                Files.writeString(readme, """
                        PlugPack
                        Клади сюда jar плагинов (plugin.yml в корне архива).
                        Это серверный лоадер для NeoForge 1.21.1. Клиентам мод не нужен.
                        NMS, Paper API и ProtocolLib тут не живут.
                        Проверка: /plugpack
                        Перечитать папку: /plugpack reload
                        """);
            }
        } catch (IOException exception) {
            PlugPack.LOGGER.error("Cannot create plugin folder {}", folder, exception);
            return 0;
        }
        VaultBridge.install(server.plugins());
        PlugPack.LOGGER.info("Scanning plugins in {}", folder.toAbsolutePath());
        List<Loaded> found = scan(folder);
        List<Loaded> ordered = order(found);
        for (Loaded plugin : ordered) {
            if (plugin.failed) continue;
            try {
                plugin.plugin.onLoad();
            } catch (Throwable thrown) {
                plugin.failed = true;
                plugin.error = thrown.toString();
                PlugPack.LOGGER.error("onLoad failed for {}", plugin.desc.getName(), thrown);
            }
        }
        int enabled = 0;
        for (Loaded plugin : ordered) {
            if (plugin.failed) continue;
            server.plugins().add(plugin.plugin);
            server.permissions().addDefaults(plugin.desc);
            server.registerPluginCommand(plugin.plugin, plugin.desc);
            plugin.plugin.setEnabled(true);
            if (plugin.plugin.isEnabled()) {
                enabled++;
                PlugPack.LOGGER.info("Enabled {} {}", plugin.desc.getName(), plugin.desc.getVersion());
            }
            loaded.add(plugin);
        }
        for (Loaded plugin : found) {
            if (plugin.failed || plugin.plugin == null || !plugin.plugin.isEnabled()) close(plugin.loader);
        }
        PlugPack.LOGGER.info("PlugPack enabled {} plugin(s) from {}", enabled, folder);
        return enabled;
    }

    public int reload() {
        disableAll();
        HandlerList.unregisterAll();
        server.tasks().cancelAll();
        server.clearPluginCommands();
        server.plugins().clear();
        for (Loaded plugin : loaded) close(plugin.loader);
        loaded.clear();
        return start(folder);
    }

    public void disableAll() {
        for (int i = loaded.size() - 1; i >= 0; i--) {
            Loaded plugin = loaded.get(i);
            if (plugin.plugin != null && plugin.plugin.isEnabled()) {
                server.plugins().unregisterAll(plugin.plugin);
                server.tasks().cancelTasks(plugin.plugin);
                plugin.plugin.setEnabled(false);
            }
            close(plugin.loader);
        }
    }

    private static void close(PluginClassLoader loader) {
        if (loader == null) return;
        try {
            loader.close();
        } catch (IOException ignored) {
        }
    }

    private List<Loaded> scan(Path folder) {
        List<Loaded> found = new ArrayList<>();
        try (Stream<Path> files = Files.list(folder)) {
            List<Path> jars = files.filter(path -> path.getFileName().toString().endsWith(".jar")).sorted().toList();
            for (Path jar : jars) {
                try {
                    PluginDescriptionFile description = readDescription(jar);
                    if ("Vault".equalsIgnoreCase(description.getName())) {
                        PlugPack.LOGGER.warn("Skipping {}: PlugPack already provides Vault services", jar.getFileName());
                        continue;
                    }
                    File data = folder.resolve(description.getName()).toFile();
                    data.mkdirs();
                    URL url = jar.toUri().toURL();
                    PluginClassLoader loader = new PluginClassLoader(url, jar.toFile(), data, description, PlugPack.class.getClassLoader());
                    Class<?> main = Class.forName(description.getMain(), true, loader);
                    if (!JavaPlugin.class.isAssignableFrom(main)) {
                        throw new IllegalStateException(description.getMain() + " does not extend JavaPlugin");
                    }
                    JavaPlugin plugin = (JavaPlugin) main.getDeclaredConstructor().newInstance();
                    Loaded loadedPlugin = new Loaded();
                    loadedPlugin.desc = description;
                    loadedPlugin.plugin = plugin;
                    loadedPlugin.loader = loader;
                    found.add(loadedPlugin);
                } catch (Throwable thrown) {
                    PlugPack.LOGGER.error("Failed to load {}: {}", jar.getFileName(), reason(thrown));
                }
            }
        } catch (IOException exception) {
            PlugPack.LOGGER.error("Failed to scan {}", folder, exception);
        }
        return found;
    }

    private static String reason(Throwable thrown) {
        StringBuilder out = new StringBuilder();
        Throwable cursor = thrown;
        int guard = 0;
        while (cursor != null && guard++ < 6) {
            if (!out.isEmpty()) out.append(" <- ");
            out.append(cursor.getClass().getSimpleName());
            if (cursor.getMessage() != null && !cursor.getMessage().isBlank()) {
                out.append(": ").append(cursor.getMessage());
            }
            cursor = cursor.getCause();
        }
        String text = out.toString();
        if (text.contains("ProtocolLib") || text.contains("com.comphenix") || text.contains("nms") || text.contains("NMS")) {
            text += ". PlugPack не умеет ProtocolLib, Paper и NMS. Такой плагин тут не заведётся.";
        }
        return text;
    }

    private static PluginDescriptionFile readDescription(Path jar) throws IOException {
        try (JarFile file = new JarFile(jar.toFile())) {
            JarEntry entry = file.getJarEntry("plugin.yml");
            if (entry == null) throw new IOException("plugin.yml missing");
            try (InputStream in = file.getInputStream(entry)) {
                String text = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return PluginDescriptionFile.from(YamlParser.parse(text));
            }
        }
    }

    private List<Loaded> order(List<Loaded> found) {
        Map<String, Loaded> byName = new HashMap<>();
        for (Loaded plugin : found) byName.put(plugin.desc.getName().toLowerCase(Locale.ROOT), plugin);
        Set<String> blocked = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Loaded plugin : found) {
                String name = plugin.desc.getName().toLowerCase(Locale.ROOT);
                if (blocked.contains(name)) continue;
                for (String depend : plugin.desc.getDepend()) {
                    Loaded dependency = byName.get(depend.toLowerCase(Locale.ROOT));
                    if (dependency == null || blocked.contains(depend.toLowerCase(Locale.ROOT))) {
                        plugin.failed = true;
                        plugin.error = "missing dependency " + depend;
                        blocked.add(name);
                        changed = true;
                        PlugPack.LOGGER.error("{} needs {}, which is not available", plugin.desc.getName(), depend);
                    }
                }
            }
        }
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, List<String>> edges = new HashMap<>();
        for (Loaded plugin : found) {
            if (plugin.failed) continue;
            String name = plugin.desc.getName().toLowerCase(Locale.ROOT);
            indegree.putIfAbsent(name, 0);
            for (String depend : concat(plugin.desc.getDepend(), plugin.desc.getSoftDepend())) {
                Loaded dependency = byName.get(depend.toLowerCase(Locale.ROOT));
                if (dependency == null || dependency.failed) continue;
                String from = depend.toLowerCase(Locale.ROOT);
                edges.computeIfAbsent(from, ignored -> new ArrayList<>()).add(name);
                indegree.merge(name, 1, Integer::sum);
                indegree.putIfAbsent(from, 0);
            }
            for (String before : plugin.desc.getLoadBefore()) {
                Loaded target = byName.get(before.toLowerCase(Locale.ROOT));
                if (target == null || target.failed) continue;
                String to = before.toLowerCase(Locale.ROOT);
                edges.computeIfAbsent(name, ignored -> new ArrayList<>()).add(to);
                indegree.merge(to, 1, Integer::sum);
                indegree.putIfAbsent(name, 0);
            }
        }
        LinkedList<String> ready = new LinkedList<>();
        indegree.entrySet().stream().filter(entry -> entry.getValue() == 0).map(Map.Entry::getKey).sorted().forEach(ready::add);
        List<Loaded> ordered = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        while (!ready.isEmpty()) {
            String name = ready.removeFirst();
            if (!seen.add(name)) continue;
            ordered.add(byName.get(name));
            for (String next : edges.getOrDefault(name, List.of())) {
                indegree.merge(next, -1, Integer::sum);
                if (indegree.get(next) == 0) ready.add(next);
            }
        }
        if (ordered.size() < indegree.size()) {
            PlugPack.LOGGER.warn("Plugin dependency cycle. Loading the rest alphabetically.");
            found.stream()
                    .filter(plugin -> !plugin.failed && !seen.contains(plugin.desc.getName().toLowerCase(Locale.ROOT)))
                    .sorted(Comparator.comparing(plugin -> plugin.desc.getName().toLowerCase(Locale.ROOT)))
                    .forEach(ordered::add);
        }
        return ordered;
    }

    private static List<String> concat(List<String> left, List<String> right) {
        List<String> all = new ArrayList<>(left);
        all.addAll(right);
        return all;
    }

    private static final class Loaded {
        PluginDescriptionFile desc;
        JavaPlugin plugin;
        PluginClassLoader loader;
        boolean failed;
        String error;
    }
}
