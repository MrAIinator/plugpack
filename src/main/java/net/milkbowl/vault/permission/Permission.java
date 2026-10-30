package net.milkbowl.vault.permission;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

public abstract class Permission {
    public abstract String getName();

    public abstract boolean isEnabled();

    public abstract boolean hasSuperPermsCompat();

    public abstract boolean playerHas(String world, String player, String permission);

    public abstract boolean playerAdd(String world, String player, String permission);

    public abstract boolean playerRemove(String world, String player, String permission);

    public abstract boolean groupHas(String world, String group, String permission);

    public abstract boolean groupAdd(String world, String group, String permission);

    public abstract boolean groupRemove(String world, String group, String permission);

    public abstract boolean playerInGroup(String world, String player, String group);

    public abstract boolean playerAddGroup(String world, String player, String group);

    public abstract boolean playerRemoveGroup(String world, String player, String group);

    public abstract String[] getPlayerGroups(String world, String player);

    public abstract String getPrimaryGroup(String world, String player);

    public abstract String[] getGroups();

    public abstract boolean hasGroupSupport();

    public boolean playerHas(String world, OfflinePlayer player, String permission) {
        return playerHas(world, player.getName(), permission);
    }

    public boolean playerHas(OfflinePlayer player, String permission) {
        return playerHas(world(player), player.getName(), permission);
    }

    public boolean playerAdd(OfflinePlayer player, String permission) {
        return playerAdd(world(player), player.getName(), permission);
    }

    public boolean playerAdd(String world, OfflinePlayer player, String permission) {
        return playerAdd(world, player.getName(), permission);
    }

    public boolean playerRemove(OfflinePlayer player, String permission) {
        return playerRemove(world(player), player.getName(), permission);
    }

    public boolean playerRemove(String world, OfflinePlayer player, String permission) {
        return playerRemove(world, player.getName(), permission);
    }

    public boolean playerInGroup(OfflinePlayer player, String group) {
        return playerInGroup(world(player), player.getName(), group);
    }

    public boolean playerInGroup(String world, OfflinePlayer player, String group) {
        return playerInGroup(world, player.getName(), group);
    }

    public boolean playerAddGroup(OfflinePlayer player, String group) {
        return playerAddGroup(world(player), player.getName(), group);
    }

    public boolean playerAddGroup(String world, OfflinePlayer player, String group) {
        return playerAddGroup(world, player.getName(), group);
    }

    public boolean playerRemoveGroup(OfflinePlayer player, String group) {
        return playerRemoveGroup(world(player), player.getName(), group);
    }

    public boolean playerRemoveGroup(String world, OfflinePlayer player, String group) {
        return playerRemoveGroup(world, player.getName(), group);
    }

    public String[] getPlayerGroups(OfflinePlayer player) {
        return getPlayerGroups(world(player), player.getName());
    }

    public String[] getPlayerGroups(String world, OfflinePlayer player) {
        return getPlayerGroups(world, player.getName());
    }

    public String getPrimaryGroup(OfflinePlayer player) {
        return getPrimaryGroup(world(player), player.getName());
    }

    public String getPrimaryGroup(String world, OfflinePlayer player) {
        return getPrimaryGroup(world, player.getName());
    }

    public boolean playerHas(World world, String player, String permission) {
        return playerHas(world == null ? null : world.getName(), player, permission);
    }

    public boolean groupHas(World world, String group, String permission) {
        return groupHas(world == null ? null : world.getName(), group, permission);
    }

    private static String world(OfflinePlayer player) {
        if (player instanceof org.bukkit.entity.Player online && online.getWorld() != null) return online.getWorld().getName();
        return null;
    }
}
