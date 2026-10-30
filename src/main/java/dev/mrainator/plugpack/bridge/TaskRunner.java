package dev.mrainator.plugpack.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.MinecraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public final class TaskRunner implements BukkitScheduler {
    private final MinecraftServer server;
    private final List<SyncTask> tasks = new ArrayList<>();
    private final List<AsyncTask> asyncTasks = new ArrayList<>();
    private final ScheduledExecutorService async = Executors.newScheduledThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "PlugPack-Async");
        thread.setDaemon(true);
        return thread;
    });
    private int nextId = 1;
    private long tick;

    public TaskRunner(MinecraftServer server) {
        this.server = server;
    }

    public void onTick() {
        tick++;
        List<SyncTask> due = new ArrayList<>();
        synchronized (tasks) {
            for (SyncTask task : tasks) {
                if (!task.cancelled && task.nextTick <= tick) due.add(task);
            }
        }
        for (SyncTask task : due) {
            if (task.cancelled || task.owner == null || !task.owner.isEnabled()) {
                task.cancelled = true;
                continue;
            }
            try {
                task.runnable.run();
            } catch (Throwable thrown) {
                dev.mrainator.plugpack.PlugPack.LOGGER.error("Task {} from {} failed", task.id, task.owner.getName(), thrown);
            }
            if (task.period > 0) task.nextTick = tick + task.period;
            else task.cancelled = true;
        }
        synchronized (tasks) {
            tasks.removeIf(task -> task.cancelled);
        }
    }

    public void shutdown() {
        async.shutdownNow();
        synchronized (tasks) {
            tasks.clear();
        }
    }

    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay) {
        return runTaskLater(plugin, task, delay).getTaskId();
    }

    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task) {
        return runTask(plugin, task).getTaskId();
    }

    @Override
    public int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
        return runTaskTimer(plugin, task, delay, period).getTaskId();
    }

    @Override
    public BukkitTask runTask(Plugin plugin, Runnable task) {
        return runTaskLater(plugin, task, 0);
    }

    @Override
    public BukkitTask runTaskLater(Plugin plugin, Runnable task, long delay) {
        return scheduleSync(plugin, task, Math.max(0, delay), 0);
    }

    @Override
    public BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        return scheduleSync(plugin, task, Math.max(0, delay), Math.max(1, period));
    }

    @Override
    public BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task) {
        return scheduleAsync(plugin, task, 0, 0);
    }

    @Override
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        return scheduleAsync(plugin, task, Math.max(0, delay), 0);
    }

    @Override
    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
        return scheduleAsync(plugin, task, Math.max(0, delay), Math.max(1, period));
    }

    @Override
    public void cancelTask(int taskId) {
        synchronized (tasks) {
            for (SyncTask task : tasks) if (task.id == taskId) task.cancelled = true;
        }
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        synchronized (tasks) {
            for (SyncTask task : tasks) if (task.owner == plugin) task.cancelled = true;
        }
        synchronized (asyncTasks) {
            for (AsyncTask task : asyncTasks) if (task.owner == plugin) task.cancel();
        }
    }

    private SyncTask scheduleSync(Plugin plugin, Runnable runnable, long delay, long period) {
        SyncTask task = new SyncTask(nextId++, plugin, runnable, tick + Math.max(1, delay), period);
        synchronized (tasks) {
            tasks.add(task);
        }
        return task;
    }

    private BukkitTask scheduleAsync(Plugin plugin, Runnable runnable, long delay, long period) {
        int id = nextId++;
        AsyncTask task = new AsyncTask(id, plugin, runnable);
        long delayMs = Math.max(0, delay) * 50L;
        task.future = period <= 0
                ? async.schedule(() -> runAsync(plugin, task), delayMs, TimeUnit.MILLISECONDS)
                : async.scheduleAtFixedRate(() -> runAsync(plugin, task), delayMs, period * 50L, TimeUnit.MILLISECONDS);
        synchronized (asyncTasks) {
            asyncTasks.add(task);
        }
        return task;
    }

    public void cancelAll() {
        synchronized (tasks) {
            for (SyncTask task : tasks) task.cancelled = true;
        }
        synchronized (asyncTasks) {
            for (AsyncTask task : asyncTasks) task.cancel();
            asyncTasks.clear();
        }
    }

    private void runAsync(Plugin plugin, AsyncTask task) {
        if (task.cancelled || plugin == null || !plugin.isEnabled()) {
            task.cancel();
            return;
        }
        try {
            task.runnable.run();
        } catch (Throwable thrown) {
            dev.mrainator.plugpack.PlugPack.LOGGER.error("Async task {} from {} failed", task.id, plugin.getName(), thrown);
        }
    }

    private final class SyncTask implements BukkitTask {
        final int id;
        final Plugin owner;
        final Runnable runnable;
        long nextTick;
        final long period;
        boolean cancelled;

        SyncTask(int id, Plugin owner, Runnable runnable, long nextTick, long period) {
            this.id = id;
            this.owner = owner;
            this.runnable = runnable;
            this.nextTick = nextTick;
            this.period = period;
        }

        @Override
        public int getTaskId() {
            return id;
        }

        @Override
        public Plugin getOwner() {
            return owner;
        }

        @Override
        public boolean isSync() {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    private final class AsyncTask implements BukkitTask {
        final int id;
        final Plugin owner;
        final Runnable runnable;
        volatile boolean cancelled;
        ScheduledFuture<?> future;

        AsyncTask(int id, Plugin owner, Runnable runnable) {
            this.id = id;
            this.owner = owner;
            this.runnable = runnable;
        }

        @Override
        public int getTaskId() {
            return id;
        }

        @Override
        public Plugin getOwner() {
            return owner;
        }

        @Override
        public boolean isSync() {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void cancel() {
            cancelled = true;
            if (future != null) future.cancel(false);
        }
    }
}
