package org.bukkit.plugin;

import java.util.Collection;
import java.util.List;

public interface ServicesManager {
    <T> void register(Class<T> service, T provider, Plugin plugin, ServicePriority priority);

    <T> void unregister(Class<T> service, T provider);

    void unregisterAll(Plugin plugin);

    <T> RegisteredServiceProvider<T> getRegistration(Class<T> service);

    <T> Collection<RegisteredServiceProvider<T>> getRegistrations(Class<T> service);

    <T> T load(Class<T> service);

    List<RegisteredServiceProvider<?>> getRegistrations(Plugin plugin);
}
