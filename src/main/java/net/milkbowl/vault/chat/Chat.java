package net.milkbowl.vault.chat;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;

public abstract class Chat {
    protected final Permission perms;

    public Chat(Permission perms) {
        this.perms = perms;
    }

    public abstract String getName();

    public abstract boolean isEnabled();

    public abstract String getPlayerPrefix(String world, String player);

    public abstract String getPlayerSuffix(String world, String player);

    public abstract void setPlayerPrefix(String world, String player, String prefix);

    public abstract void setPlayerSuffix(String world, String player, String suffix);

    public abstract String getGroupPrefix(String world, String group);

    public abstract String getGroupSuffix(String world, String group);

    public abstract void setGroupPrefix(String world, String group, String prefix);

    public abstract void setGroupSuffix(String world, String group, String suffix);

    public String getPlayerPrefix(String world, OfflinePlayer player) {
        return getPlayerPrefix(world, player.getName());
    }

    public String getPlayerSuffix(String world, OfflinePlayer player) {
        return getPlayerSuffix(world, player.getName());
    }

    public void setPlayerPrefix(String world, OfflinePlayer player, String prefix) {
        setPlayerPrefix(world, player.getName(), prefix);
    }

    public void setPlayerSuffix(String world, OfflinePlayer player, String suffix) {
        setPlayerSuffix(world, player.getName(), suffix);
    }

    public Permission getPermissionProvider() {
        return perms;
    }
}
