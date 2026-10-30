package org.bukkit;

import java.util.UUID;
import org.bukkit.entity.Player;

public interface OfflinePlayer {
    UUID getUniqueId();

    String getName();

    boolean isOnline();

    boolean isOp();

    void setOp(boolean value);

    Player getPlayer();

    boolean hasPlayedBefore();

    long getFirstPlayed();

    long getLastPlayed();
}
