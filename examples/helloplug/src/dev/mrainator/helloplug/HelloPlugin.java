package dev.mrainator.helloplug;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class HelloPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("HelloPlug включён на PlugPack");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String greeting = getConfig().getString("greeting", "Сервер на NeoForge, плагин живой.");
        event.getPlayer().sendMessage(ChatColor.GOLD + greeting);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int count = Bukkit.getPluginManager().getPlugins().length;
        sender.sendMessage(ChatColor.GREEN + "PlugPack живой. Плагинов: " + count);
        if (sender instanceof Player player) {
            player.sendMessage(ChatColor.GRAY + "Ты: " + player.getName() + ", мир " + player.getWorld().getName());
        }
        return true;
    }
}
