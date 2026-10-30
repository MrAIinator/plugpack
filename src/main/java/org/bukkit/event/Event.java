package org.bukkit.event;

public abstract class Event {
    private final boolean async;
    private String name;

    public Event() {
        this(false);
    }

    public Event(boolean async) {
        this.async = async;
    }

    public boolean isAsynchronous() {
        return async;
    }

    public String getEventName() {
        if (name == null) name = getClass().getSimpleName();
        return name;
    }

    public abstract HandlerList getHandlers();
}
