package dev.mrainator.plugpack.bridge;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import com.mojang.brigadier.CommandDispatcher;
import dev.mrainator.plugpack.PlugPack;
import dev.mrainator.plugpack.PlugPackConfig;
import dev.mrainator.plugpack.load.PluginHost;
import dev.mrainator.plugpack.optools.OpTools;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@EventBusSubscriber(modid = PlugPack.MOD_ID, value = Dist.DEDICATED_SERVER)
public final class ServerHooks {
    private static NeoServer runtime;
    private static PluginHost host;
    private static CommandDispatcher<CommandSourceStack> dispatcher;

    private ServerHooks() {
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        dispatcher = event.getDispatcher();
        if (runtime != null) runtime.rememberDispatcher(dispatcher);
    }

    @SubscribeEvent
    public static void onStarting(ServerStartingEvent event) {
        if (!PlugPackConfig.ENABLED.get()) {
            PlugPack.LOGGER.info("PlugPack is disabled in config.");
            return;
        }
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path plugins = gameDir.resolve(PlugPackConfig.FOLDER.get());
        Path permissions = gameDir.resolve("config").resolve("plugpack").resolve("permissions.txt");
        runtime = new NeoServer(event.getServer(), permissions, PlugPackConfig.OP_ALL.get());
        Bukkit.setServer(runtime);
        CommandDispatcher<CommandSourceStack> active = dispatcher != null ? dispatcher : event.getServer().getCommands().getDispatcher();
        runtime.rememberDispatcher(active);
        host = new PluginHost(runtime);
        runtime.attach(host);
        host.start(plugins);
    }

    @SubscribeEvent
    public static void onStopping(ServerStoppingEvent event) {
        if (host != null) host.disableAll();
        if (runtime != null) runtime.tasks().shutdown();
        host = null;
        runtime = null;
        Bukkit.setServer(null);
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        if (runtime == null) return;
        runtime.tasks().onTick();
        if (PlugPackConfig.MOVE_EVENTS.get()) runtime.tickMoves();
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (runtime == null || !(event.getEntity() instanceof ServerPlayer player)) return;
        NeoPlayer wrapped = runtime.wrap(player);
        PlayerJoinEvent join = new PlayerJoinEvent(wrapped, null);
        runtime.plugins().callEvent(join);
        if (join.getJoinMessage() != null) runtime.broadcastMessage(join.getJoinMessage());
    }

    @SubscribeEvent
    public static void onQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (runtime == null || !(event.getEntity() instanceof ServerPlayer player)) return;
        NeoPlayer wrapped = runtime.wrap(player);
        PlayerQuitEvent quit = new PlayerQuitEvent(wrapped, null);
        runtime.plugins().callEvent(quit);
        if (quit.getQuitMessage() != null) runtime.broadcastMessage(quit.getQuitMessage());
        runtime.forget(player.getUUID());
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        if (runtime == null) return;
        NeoPlayer player = runtime.wrap(event.getPlayer());
        Set<org.bukkit.entity.Player> recipients = new HashSet<>(runtime.getOnlinePlayers());
        AsyncPlayerChatEvent chat = new AsyncPlayerChatEvent(false, player, event.getRawText(), recipients);
        runtime.plugins().callEvent(chat);
        if (chat.isCancelled()) {
            event.setCanceled(true);
            return;
        }
        if (chat.getMessage() != null && !chat.getMessage().equals(event.getRawText())) {
            event.setMessage(dev.mrainator.plugpack.bridge.LegacyText.toComponent(chat.getMessage()));
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        if (runtime == null) return;
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player)) return;
        String raw = event.getParseResults().getReader().getString();
        String message = raw.startsWith("/") ? raw : "/" + raw;
        PlayerCommandPreprocessEvent preprocess = new PlayerCommandPreprocessEvent(runtime.wrap(player), message);
        runtime.plugins().callEvent(preprocess);
        if (preprocess.isCancelled()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (runtime == null || !(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (OpTools.blocked(player, level, event.getPos())) {
            event.setCanceled(true);
            return;
        }
        org.bukkit.block.Block block = runtime.world(level).getBlockAt(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
        BlockBreakEvent broken = new BlockBreakEvent(block, runtime.wrap(player));
        runtime.plugins().callEvent(broken);
        if (broken.isCancelled()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (runtime == null || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (OpTools.blocked(player, level, event.getPos())) {
            event.setCanceled(true);
            return;
        }
        NeoPlayer wrapped = runtime.wrap(player);
        org.bukkit.block.Block block = runtime.world(level).getBlockAt(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
        BlockPlaceEvent placed = new BlockPlaceEvent(block, wrapped, wrapped.getItemInHand());
        runtime.plugins().callEvent(placed);
        if (placed.isCancelled() || !placed.canBuild()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightBlock(PlayerInteractEvent.RightClickBlock event) {
        interact(event, org.bukkit.event.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, false);
    }

    @SubscribeEvent
    public static void onLeftBlock(PlayerInteractEvent.LeftClickBlock event) {
        interact(event, org.bukkit.event.player.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, false);
    }

    @SubscribeEvent
    public static void onRightItem(PlayerInteractEvent.RightClickItem event) {
        interact(event, org.bukkit.event.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR, true);
    }

    @SubscribeEvent
    public static void onLeftAir(PlayerInteractEvent.LeftClickEmpty event) {
        interact(event, org.bukkit.event.player.PlayerInteractEvent.Action.LEFT_CLICK_AIR, true);
    }

    private static void interact(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent event, org.bukkit.event.player.PlayerInteractEvent.Action action, boolean air) {
        if (runtime == null || !(event.getEntity() instanceof ServerPlayer player)) return;
        boolean left = event instanceof net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
        if (!air && OpTools.onAxe(player, event.getPos(), left)) {
            cancelInteract(event);
            return;
        }
        if (!air && OpTools.blocked(player, player.serverLevel(), event.getPos())) {
            cancelInteract(event);
            return;
        }
        if (!(player.serverLevel() instanceof ServerLevel level)) return;
        NeoPlayer wrapped = runtime.wrap(player);
        org.bukkit.block.Block block = air ? null : runtime.world(level).getBlockAt(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
        org.bukkit.event.player.PlayerInteractEvent interact = new org.bukkit.event.player.PlayerInteractEvent(wrapped, action, wrapped.getItemInHand(), block);
        runtime.plugins().callEvent(interact);
        if (!interact.isCancelled()) return;
        cancelInteract(event);
    }

    private static void cancelInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent event) {
        if (event instanceof net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock click) {
            click.setCanceled(true);
            click.setCancellationResult(InteractionResult.FAIL);
        } else if (event instanceof net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock click) {
            click.setCanceled(true);
        } else if (event instanceof net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem click) {
            click.setCanceled(true);
            click.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (runtime == null || !(event.getEntity() instanceof ServerPlayer player)) return;
        String message;
        try {
            message = player.getCombatTracker().getDeathMessage().getString();
        } catch (Throwable ignored) {
            message = player.getGameProfile().getName() + " died";
        }
        runtime.plugins().callEvent(new PlayerDeathEvent(runtime.wrap(player), message));
    }
}
