package org.bukkit.scheduler;

import org.bukkit.plugin.Plugin;

public interface BukkitScheduler {
    int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay);

    int scheduleSyncDelayedTask(Plugin plugin, Runnable task);

    int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period);

    BukkitTask runTask(Plugin plugin, Runnable task);

    BukkitTask runTaskLater(Plugin plugin, Runnable task, long delay);

    BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period);

    BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task);

    BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay);

    BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period);

    void cancelTask(int taskId);

    void cancelTasks(Plugin plugin);
}
