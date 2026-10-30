package org.bukkit.entity;

import java.net.InetSocketAddress;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public interface Player extends CommandSender, OfflinePlayer, Entity {
    void sendMessage(String message);

    void sendMessage(String[] messages);

    void sendMessage(net.md_5.bungee.api.chat.BaseComponent component);

    void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components);

    String getDisplayName();

    void setDisplayName(String name);

    boolean isOnline();

    boolean isOp();

    void setOp(boolean value);

    GameMode getGameMode();

    void setGameMode(GameMode mode);

    double getHealth();

    void setHealth(double health);

    double getMaxHealth();

    int getFoodLevel();

    void setFoodLevel(int level);

    int getLevel();

    void setLevel(int level);

    float getExp();

    void setExp(float exp);

    void giveExp(int amount);

    PlayerInventory getInventory();

    ItemStack getItemInHand();

    void kickPlayer(String reason);

    void chat(String message);

    boolean performCommand(String command);

    InetSocketAddress getAddress();

    boolean isSneaking();

    boolean isSprinting();

    void setSneaking(boolean sneak);

    void setSprinting(boolean sprint);

    Location getLocation();
}
