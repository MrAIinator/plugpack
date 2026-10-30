package org.bukkit.command;

import org.bukkit.Server;
import org.bukkit.permissions.Permissible;

public interface CommandSender extends Permissible {
    void sendMessage(String message);

    void sendMessage(String[] messages);

    void sendMessage(net.md_5.bungee.api.chat.BaseComponent component);

    void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components);

    Server getServer();

    String getName();

    boolean isOp();

    void setOp(boolean value);

    Spigot spigot();

    class Spigot {
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
        }

        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
        }
    }
}
