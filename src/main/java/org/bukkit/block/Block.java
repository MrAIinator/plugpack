package org.bukkit.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public interface Block {
    Material getType();

    void setType(Material type);

    Location getLocation();

    World getWorld();

    int getX();

    int getY();

    int getZ();

    boolean isEmpty();

    Block getRelative(BlockFace face);

    Block getRelative(int modX, int modY, int modZ);
}
