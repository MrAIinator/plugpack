package org.bukkit.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class BukkitRunnable implements Runnable {
    private BukkitTask task;

    public synchronized void cancel() {
        if (task == null) throw new IllegalStateException("Task never scheduled");
        task.cancel();
    }

    public synchronized BukkitTask runTask(Plugin plugin) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTask(plugin, this);
    }

    public synchronized BukkitTask runTaskLater(Plugin plugin, long delay) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTaskLater(plugin, this, delay);
    }

    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period);
    }

    public synchronized BukkitTask runTaskAsynchronously(Plugin plugin) {
        checkNotYetScheduled();
        return task = Bukkit.getScheduler().runTaskAsynchronously(plugin, this);
    }

    public synchronized BukkitTask getTask() {
        return task;
    }

    private void checkNotYetScheduled() {
        if (task != null) throw new IllegalStateException("Already scheduled");
    }
}
