package org.bukkit.event;

import java.lang.reflect.Method;
import org.bukkit.plugin.Plugin;

public class RegisteredListener {
    private final Listener listener;
    private final Method method;
    private final Plugin plugin;
    private final EventPriority priority;
    private final boolean ignoreCancelled;

    public RegisteredListener(Listener listener, Method method, EventPriority priority, Plugin plugin, boolean ignoreCancelled) {
        this.listener = listener;
        this.method = method;
        this.priority = priority;
        this.plugin = plugin;
        this.ignoreCancelled = ignoreCancelled;
        method.setAccessible(true);
    }

    public Listener getListener() {
        return listener;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public boolean isIgnoringCancelled() {
        return ignoreCancelled;
    }

    public void callEvent(Event event) throws Exception {
        if (ignoreCancelled && event instanceof Cancellable cancellable && cancellable.isCancelled()) return;
        method.invoke(listener, event);
    }
}
