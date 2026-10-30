package org.bukkit;

import java.util.List;
import java.util.UUID;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface World {
    String getName();

    UUID getUID();

    Block getBlockAt(int x, int y, int z);

    Block getBlockAt(Location location);

    List<Player> getPlayers();

    long getTime();

    void setTime(long time);

    long getFullTime();

    Location getSpawnLocation();
}
