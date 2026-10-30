package org.bukkit.entity;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;

public interface Entity {
    UUID getUniqueId();

    String getName();

    Location getLocation();

    World getWorld();

    boolean teleport(Location location);

    boolean isDead();

    boolean isValid();
}
