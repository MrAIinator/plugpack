package org.bukkit;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

public final class Bukkit {
    private static Server server;

    private Bukkit() {
    }

    public static void setServer(Server server) {
        Bukkit.server = server;
    }

    public static Server getServer() {
        if (server == null) throw new IllegalStateException("PlugPack server is not running");
        return server;
    }

    public static String getName() {
        return getServer().getName();
    }

    public static String getVersion() {
        return getServer().getVersion();
    }

    public static String getBukkitVersion() {
        return getServer().getBukkitVersion();
    }

    public static Collection<? extends Player> getOnlinePlayers() {
        return getServer().getOnlinePlayers();
    }

    public static Player getPlayer(String name) {
        return getServer().getPlayer(name);
    }

    public static Player getPlayerExact(String name) {
        return getServer().getPlayerExact(name);
    }

    public static Player getPlayer(UUID id) {
        return getServer().getPlayer(id);
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        return getServer().getOfflinePlayer(name);
    }

    public static OfflinePlayer getOfflinePlayer(UUID id) {
        return getServer().getOfflinePlayer(id);
    }

    public static World getWorld(String name) {
        return getServer().getWorld(name);
    }

    public static java.util.List<World> getWorlds() {
        return getServer().getWorlds();
    }

    public static PluginManager getPluginManager() {
        return getServer().getPluginManager();
    }

    public static ServicesManager getServicesManager() {
        return getServer().getServicesManager();
    }

    public static BukkitScheduler getScheduler() {
        return getServer().getScheduler();
    }

    public static Messenger getMessenger() {
        return getServer().getMessenger();
    }

    public static Logger getLogger() {
        return getServer().getLogger();
    }

    public static ConsoleCommandSender getConsoleSender() {
        return getServer().getConsoleSender();
    }

    public static PluginCommand getPluginCommand(String name) {
        return getServer().getPluginCommand(name);
    }

    public static boolean dispatchCommand(CommandSender sender, String commandLine) {
        return getServer().dispatchCommand(sender, commandLine);
    }

    public static int broadcastMessage(String message) {
        return getServer().broadcastMessage(message);
    }

    public static Plugin getPlugin(String name) {
        return getPluginManager().getPlugin(name);
    }
}
