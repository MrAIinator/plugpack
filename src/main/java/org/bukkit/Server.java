package org.bukkit;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;

public interface Server {
    String getName();

    String getVersion();

    String getBukkitVersion();

    String getMinecraftVersion();

    int getMaxPlayers();

    int getPort();

    String getIp();

    String getMotd();

    Collection<? extends Player> getOnlinePlayers();

    int getOnlineCount();

    Player getPlayer(String name);

    Player getPlayerExact(String name);

    Player getPlayer(UUID id);

    List<Player> matchPlayer(String name);

    OfflinePlayer getOfflinePlayer(String name);

    OfflinePlayer getOfflinePlayer(UUID id);

    World getWorld(String name);

    List<World> getWorlds();

    PluginManager getPluginManager();

    ServicesManager getServicesManager();

    BukkitScheduler getScheduler();

    Messenger getMessenger();

    Logger getLogger();

    ConsoleCommandSender getConsoleSender();

    CommandSender getConsole();

    boolean dispatchCommand(CommandSender sender, String commandLine);

    int broadcastMessage(String message);

    int broadcast(String message, String permission);

    PluginCommand getPluginCommand(String name);

    GameMode getDefaultGameMode();

    void shutdown();
}
