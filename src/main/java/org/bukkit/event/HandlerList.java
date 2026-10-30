package org.bukkit.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.plugin.Plugin;

public class HandlerList {
    private static final List<HandlerList> ALL = new ArrayList<>();

    private final Map<EventPriority, List<RegisteredListener>> handlers = new EnumMap<>(EventPriority.class);
    private RegisteredListener[] baked;

    public HandlerList() {
        for (EventPriority priority : EventPriority.values()) handlers.put(priority, new ArrayList<>());
        synchronized (ALL) {
            ALL.add(this);
        }
    }

    public synchronized void register(RegisteredListener listener) {
        if (listener.getPriority() == null) return;
        handlers.get(listener.getPriority()).add(listener);
        baked = null;
    }

    public synchronized void unregister(Plugin plugin) {
        for (List<RegisteredListener> list : handlers.values()) {
            list.removeIf(listener -> listener.getPlugin().equals(plugin));
        }
        baked = null;
    }

    public synchronized void unregister(Listener listener) {
        for (List<RegisteredListener> list : handlers.values()) {
            list.removeIf(registered -> registered.getListener() == listener);
        }
        baked = null;
    }

    public synchronized RegisteredListener[] getRegisteredListeners() {
        if (baked != null) return baked;
        List<RegisteredListener> ordered = new ArrayList<>();
        for (EventPriority priority : EventPriority.values()) ordered.addAll(handlers.get(priority));
        baked = ordered.toArray(RegisteredListener[]::new);
        return baked;
    }

    public static void unregisterAll(Plugin plugin) {
        synchronized (ALL) {
            for (HandlerList list : ALL) list.unregister(plugin);
        }
    }

    public static void unregisterAll() {
        synchronized (ALL) {
            for (HandlerList list : ALL) {
                synchronized (list) {
                    for (List<RegisteredListener> handlers : list.handlers.values()) handlers.clear();
                    list.baked = null;
                }
            }
        }
    }
}
