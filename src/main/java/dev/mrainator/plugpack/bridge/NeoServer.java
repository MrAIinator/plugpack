package dev.mrainator.plugpack.bridge;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrainator.plugpack.PlugPack;
import dev.mrainator.plugpack.load.PluginHost;
import dev.mrainator.plugpack.load.SimplePluginManager;
import dev.mrainator.plugpack.optools.OpTools;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

public final class NeoServer implements Server {
    private final MinecraftServer minecraft;
    private final SimplePluginManager plugins = new SimplePluginManager();
    private final TaskRunner tasks;
    private final Permissions permissions;
    private final NeoConsole console = new NeoConsole(this);
    private final Map<UUID, NeoPlayer> players = new HashMap<>();
    private final Map<String, NeoWorld> worlds = new HashMap<>();
    private final Map<UUID, NeoOffline> offline = new HashMap<>();
    private final Map<String, PluginCommand> commands = new HashMap<>();
    private final Map<UUID, BlockPos> moveTracker = new HashMap<>();
    private PluginHost host;
    private CommandDispatcher<CommandSourceStack> dispatcher;

    public NeoServer(MinecraftServer minecraft, java.nio.file.Path permissionFile, boolean opHasAll) {
        this.minecraft = minecraft;
        this.tasks = new TaskRunner(minecraft);
        this.permissions = new Permissions(permissionFile, opHasAll);
    }

    public MinecraftServer minecraft() {
        return minecraft;
    }

    public SimplePluginManager plugins() {
        return plugins;
    }

    public TaskRunner tasks() {
        return tasks;
    }

    public Permissions permissions() {
        return permissions;
    }

    public void attach(PluginHost host) {
        this.host = host;
    }

    public void rememberDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.dispatcher = dispatcher;
        ensurePlugpack(dispatcher);
        for (PluginCommand command : new ArrayList<>(new java.util.HashSet<>(commands.values()))) bind(dispatcher, command);
    }

    NeoPlayer wrap(ServerPlayer player) {
        NeoPlayer wrapped = players.computeIfAbsent(player.getUUID(), id -> new NeoPlayer(this, id, player.getGameProfile().getName()));
        wrapped.name = player.getGameProfile().getName();
        return wrapped;
    }

    NeoWorld world(ServerLevel level) {
        String name = worldName(level);
        return worlds.computeIfAbsent(name, ignored -> new NeoWorld(this, level, name));
    }

    public void registerPluginCommand(JavaPlugin plugin, PluginDescriptionFile description) {
        for (Map.Entry<String, Map<String, Object>> entry : description.getCommands().entrySet()) {
            PluginCommand command = new PluginCommand(entry.getKey().toLowerCase(Locale.ROOT), plugin);
            Map<String, Object> section = entry.getValue();
            if (section.get("description") != null) command.setDescription(String.valueOf(section.get("description")));
            if (section.get("usage") != null) command.setUsage(String.valueOf(section.get("usage")));
            if (section.get("permission") != null) command.setPermission(String.valueOf(section.get("permission")));
            if (section.get("permission-message") != null) command.setPermissionMessage(String.valueOf(section.get("permission-message")));
            command.setAliases(stringList(section.get("aliases")));
            commands.put(command.getName(), command);
            for (String alias : command.getAliases()) commands.put(alias.toLowerCase(Locale.ROOT), command);
            if (dispatcher != null) bind(dispatcher, command);
        }
    }

    public void clearPluginCommands() {
        commands.clear();
    }

    public void tickMoves() {
        for (ServerPlayer player : minecraft.getPlayerList().getPlayers()) {
            BlockPos now = player.blockPosition();
            BlockPos previous = moveTracker.put(player.getUUID(), now);
            if (previous == null || previous.equals(now)) continue;
            NeoPlayer wrapped = wrap(player);
            org.bukkit.event.player.PlayerMoveEvent event = new org.bukkit.event.player.PlayerMoveEvent(
                    wrapped,
                    new org.bukkit.Location(world(player.serverLevel()), previous.getX() + 0.5, previous.getY(), previous.getZ() + 0.5),
                    wrapped.getLocation()
            );
            plugins.callEvent(event);
            if (event.isCancelled()) {
                player.teleportTo(player.serverLevel(), previous.getX() + 0.5, previous.getY(), previous.getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot());
                moveTracker.put(player.getUUID(), previous);
            }
        }
    }

    public void forget(UUID id) {
        moveTracker.remove(id);
    }

    private void ensurePlugpack(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (dispatcher.getRoot().getChild("plugpack") != null) return;
        dispatcher.register(Commands.literal("plugpack")
                .executes(context -> {
                    send(context.getSource(), status());
                    return 1;
                })
                .then(Commands.literal("plugins").executes(context -> {
                    send(context.getSource(), status());
                    return 1;
                }))
                .then(Commands.literal("reload").requires(source -> source.hasPermission(2)).executes(context -> {
                    if (host == null) return 0;
                    int count = host.reload();
                    send(context.getSource(), "Reloaded " + count + " plugin(s). Restart the server if a plugin changed its classes.");
                    return 1;
                })));
        OpTools.register(dispatcher);
    }

    private String status() {
        StringBuilder out = new StringBuilder("PlugPack plugins: ");
        org.bukkit.plugin.Plugin[] loaded = plugins.getPlugins();
        if (loaded.length == 0) return "PlugPack: no plugins loaded.";
        for (int i = 0; i < loaded.length; i++) {
            if (i > 0) out.append(", ");
            out.append(loaded[i].isEnabled() ? "\u00A7a" : "\u00A7c").append(loaded[i].getName()).append("\u00A7r");
        }
        return out.toString();
    }

    private static void send(CommandSourceStack source, String message) {
        source.sendSuccess(() -> LegacyText.toComponent(message), false);
    }

    private void bind(CommandDispatcher<CommandSourceStack> dispatcher, PluginCommand command) {
        if (dispatcher.getRoot().getChild(command.getName()) != null) return;
        var node = dispatcher.register(Commands.literal(command.getName())
                .executes(context -> execute(command, context, new String[0]))
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests((context, builder) -> suggest(command, context.getSource(), builder))
                        .executes(context -> execute(command, context, split(StringArgumentType.getString(context, "args"))))));
        for (String alias : command.getAliases()) {
            if (dispatcher.getRoot().getChild(alias) == null) {
                dispatcher.register(Commands.literal(alias).redirect(node));
            }
        }
    }

    private int execute(PluginCommand command, com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, String[] args) {
        CommandSender sender = senderOf(context.getSource());
        return command.execute(sender, command.getName(), args) ? 1 : 0;
    }

    private CompletableFuture<Suggestions> suggest(PluginCommand command, CommandSourceStack source, SuggestionsBuilder builder) {
        String[] args = split(builder.getRemaining());
        List<String> options = command.tabComplete(senderOf(source), command.getName(), args);
        if (options != null) {
            String token = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
            for (String option : options) {
                if (option != null && option.toLowerCase(Locale.ROOT).startsWith(token)) builder.suggest(option);
            }
        }
        return builder.buildFuture();
    }

    private CommandSender senderOf(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) return wrap(player);
        return console;
    }

    private static String[] split(String raw) {
        if (raw == null || raw.isBlank()) return new String[0];
        return raw.trim().split("\\s+");
    }

    private static List<String> stringList(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) if (item != null) out.add(String.valueOf(item));
        } else if (value instanceof String text && !text.isBlank()) {
            for (String part : text.split(",")) if (!part.isBlank()) out.add(part.trim());
        }
        return out;
    }

    private static String worldName(ServerLevel level) {
        String id = level.dimension().location().toString();
        if ("minecraft:overworld".equals(id)) return "world";
        if ("minecraft:the_nether".equals(id)) return "world_nether";
        if ("minecraft:the_end".equals(id)) return "world_the_end";
        return id.replace(':', '_');
    }

    @Override
    public String getName() {
        return "PlugPack";
    }

    @Override
    public String getVersion() {
        return "1.21.1-NeoForge-21.1.249 (PlugPack)";
    }

    @Override
    public String getBukkitVersion() {
        return "1.21.1-R0.1-SNAPSHOT";
    }

    @Override
    public String getMinecraftVersion() {
        return "1.21.1";
    }

    @Override
    public int getMaxPlayers() {
        return minecraft.getMaxPlayers();
    }

    @Override
    public int getPort() {
        return minecraft.getPort();
    }

    @Override
    public String getIp() {
        return "";
    }

    @Override
    public String getMotd() {
        return minecraft.getMotd();
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();
        for (ServerPlayer player : minecraft.getPlayerList().getPlayers()) online.add(wrap(player));
        return online;
    }

    @Override
    public int getOnlineCount() {
        return minecraft.getPlayerCount();
    }

    @Override
    public Player getPlayer(String name) {
        if (name == null) return null;
        Player exact = getPlayerExact(name);
        if (exact != null) return exact;
        String lower = name.toLowerCase(Locale.ROOT);
        Player found = null;
        for (Player player : getOnlinePlayers()) {
            if (player.getName() != null && player.getName().toLowerCase(Locale.ROOT).startsWith(lower)) {
                if (found != null) return null;
                found = player;
            }
        }
        return found;
    }

    @Override
    public Player getPlayerExact(String name) {
        if (name == null) return null;
        ServerPlayer player = minecraft.getPlayerList().getPlayerByName(name);
        return player == null ? null : wrap(player);
    }

    @Override
    public Player getPlayer(UUID id) {
        if (id == null) return null;
        ServerPlayer player = minecraft.getPlayerList().getPlayer(id);
        return player == null ? null : wrap(player);
    }

    @Override
    public List<Player> matchPlayer(String name) {
        List<Player> found = new ArrayList<>();
        if (name == null) return found;
        String lower = name.toLowerCase(Locale.ROOT);
        for (Player player : getOnlinePlayers()) {
            if (player.getName() != null && player.getName().toLowerCase(Locale.ROOT).startsWith(lower)) found.add(player);
        }
        return found;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        if (name == null) return null;
        Player online = getPlayerExact(name);
        if (online != null) return online;
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        NeoOffline stored = offline.get(id);
        if (stored == null) {
            stored = new NeoOffline(this, id, name);
            offline.put(id, stored);
        }
        return stored;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        if (id == null) return null;
        Player online = getPlayer(id);
        if (online != null) return online;
        return offline.computeIfAbsent(id, key -> new NeoOffline(this, key, null));
    }

    @Override
    public World getWorld(String name) {
        if (name == null) return null;
        for (ServerLevel level : minecraft.getAllLevels()) {
            NeoWorld world = world(level);
            if (world.getName().equalsIgnoreCase(name)) return world;
        }
        return worlds.get(name);
    }

    @Override
    public List<World> getWorlds() {
        List<World> list = new ArrayList<>();
        for (ServerLevel level : minecraft.getAllLevels()) list.add(world(level));
        return list;
    }

    @Override
    public PluginManager getPluginManager() {
        return plugins;
    }

    @Override
    public ServicesManager getServicesManager() {
        return plugins;
    }

    @Override
    public BukkitScheduler getScheduler() {
        return tasks;
    }

    @Override
    public Messenger getMessenger() {
        return plugins;
    }

    @Override
    public Logger getLogger() {
        Logger logger = Logger.getLogger("PlugPack");
        logger.setUseParentHandlers(false);
        return logger;
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return console;
    }

    @Override
    public CommandSender getConsole() {
        return console;
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) {
        if (commandLine == null) return false;
        String raw = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;
        CommandSourceStack source;
        if (sender instanceof NeoPlayer player && player.online() != null) source = player.online().createCommandSourceStack();
        else source = minecraft.createCommandSourceStack();
        minecraft.getCommands().performPrefixedCommand(source, raw);
        return true;
    }

    @Override
    public int broadcastMessage(String message) {
        return broadcast(message, null);
    }

    @Override
    public int broadcast(String message, String permission) {
        int count = 0;
        for (Player player : getOnlinePlayers()) {
            if (permission == null || player.hasPermission(permission)) {
                player.sendMessage(message);
                count++;
            }
        }
        PlugPack.LOGGER.info(LegacyText.plain(message));
        return count;
    }

    @Override
    public PluginCommand getPluginCommand(String name) {
        if (name == null) return null;
        return commands.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public GameMode getDefaultGameMode() {
        GameType type = minecraft.getDefaultGameType();
        return switch (type) {
            case CREATIVE -> GameMode.CREATIVE;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
            default -> GameMode.SURVIVAL;
        };
    }

    @Override
    public void shutdown() {
        minecraft.halt(false);
    }

    static {
        Bukkit.setServer(null);
    }
}
