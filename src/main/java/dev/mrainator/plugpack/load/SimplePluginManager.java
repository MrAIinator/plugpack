package dev.mrainator.plugpack.load;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import dev.mrainator.plugpack.PlugPack;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.RegisteredListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class SimplePluginManager implements PluginManager, PluginLoader, ServicesManager, Messenger {
    private final List<Plugin> plugins = new ArrayList<>();
    private final List<RegisteredServiceProvider<?>> services = new ArrayList<>();
    private final Set<String> channels = new HashSet<>();

    public void add(Plugin plugin) {
        plugins.removeIf(existing -> existing.getName().equalsIgnoreCase(plugin.getName()));
        plugins.add(plugin);
    }

    public void clear() {
        plugins.clear();
        services.clear();
        channels.clear();
    }

    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        if (listener == null || plugin == null) return;
        for (Class<?> type = listener.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                EventHandler annotation = method.getAnnotation(EventHandler.class);
                if (annotation == null || method.isSynthetic() || method.getParameterCount() != 1) continue;
                Class<?> parameter = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(parameter)) continue;
                try {
                    Method listMethod = parameter.getMethod("getHandlerList");
                    Object value = listMethod.invoke(null);
                    if (value instanceof HandlerList list) {
                        list.register(new RegisteredListener(listener, method, annotation.priority(), plugin, annotation.ignoreCancelled()));
                    }
                } catch (ReflectiveOperationException exception) {
                    PlugPack.LOGGER.warn("Cannot register {}#{} for {}: {}", listener.getClass().getSimpleName(), method.getName(), parameter.getSimpleName(), exception.toString());
                }
            }
        }
    }

    @Override
    public void callEvent(Event event) {
        if (event == null) return;
        for (RegisteredListener listener : event.getHandlers().getRegisteredListeners()) {
            if (listener.getPlugin() != null && !listener.getPlugin().isEnabled()) continue;
            try {
                listener.callEvent(event);
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                PlugPack.LOGGER.error("Could not pass {} to {}", event.getEventName(), listener.getPlugin().getName(), cause);
            } catch (Throwable thrown) {
                PlugPack.LOGGER.error("Could not pass {} to {}", event.getEventName(), listener.getPlugin().getName(), thrown);
            }
        }
    }

    @Override
    public Plugin getPlugin(String name) {
        if (name == null) return null;
        for (Plugin plugin : plugins) if (plugin.getName().equalsIgnoreCase(name)) return plugin;
        return null;
    }

    @Override
    public Plugin[] getPlugins() {
        return plugins.toArray(Plugin[]::new);
    }

    @Override
    public boolean isPluginEnabled(String name) {
        Plugin plugin = getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public boolean isPluginEnabled(Plugin plugin) {
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (plugin instanceof org.bukkit.plugin.java.JavaPlugin javaPlugin) javaPlugin.setEnabled(true);
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (plugin instanceof org.bukkit.plugin.java.JavaPlugin javaPlugin && javaPlugin.isEnabled()) javaPlugin.setEnabled(false);
    }

    @Override
    public <T> void register(Class<T> service, T provider, Plugin plugin, ServicePriority priority) {
        services.add(new RegisteredServiceProvider<>(service, provider, priority, plugin));
    }

    @Override
    public <T> void unregister(Class<T> service, T provider) {
        services.removeIf(entry -> entry.getService() == service && entry.getProvider() == provider);
    }

    @Override
    public void unregisterAll(Plugin plugin) {
        services.removeIf(entry -> entry.getPlugin() == plugin);
        HandlerList.unregisterAll(plugin);
        channels.removeIf(channel -> channel.startsWith(plugin.getName() + ":"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RegisteredServiceProvider<T> getRegistration(Class<T> service) {
        return services.stream()
                .filter(entry -> entry.getService() == service)
                .max(Comparator.comparingInt(entry -> entry.getPriority().ordinal()))
                .map(entry -> (RegisteredServiceProvider<T>) entry)
                .orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<RegisteredServiceProvider<T>> getRegistrations(Class<T> service) {
        List<RegisteredServiceProvider<T>> found = new ArrayList<>();
        for (RegisteredServiceProvider<?> entry : services) {
            if (entry.getService() == service) found.add((RegisteredServiceProvider<T>) entry);
        }
        return found;
    }

    @Override
    public <T> T load(Class<T> service) {
        RegisteredServiceProvider<T> registration = getRegistration(service);
        return registration == null ? null : registration.getProvider();
    }

    @Override
    public List<RegisteredServiceProvider<?>> getRegistrations(Plugin plugin) {
        List<RegisteredServiceProvider<?>> found = new ArrayList<>();
        for (RegisteredServiceProvider<?> entry : services) if (entry.getPlugin() == plugin) found.add(entry);
        return found;
    }

    @Override
    public void registerOutgoingPluginChannel(Plugin plugin, String channel) {
        if (plugin != null && channel != null) channels.add(plugin.getName() + ":" + channel);
    }

    @Override
    public void unregisterOutgoingPluginChannel(Plugin plugin, String channel) {
        if (plugin != null) channels.remove(plugin.getName() + ":" + channel);
    }

    @Override
    public void unregisterOutgoingPluginChannel(Plugin plugin) {
        if (plugin != null) channels.removeIf(entry -> entry.startsWith(plugin.getName() + ":"));
    }

    @Override
    public boolean isOutgoingChannelRegistered(Plugin plugin, String channel) {
        return plugin != null && channels.contains(plugin.getName() + ":" + channel);
    }

    @Override
    public void registerIncomingPluginChannel(Plugin plugin, String channel, PluginMessageListener listener) {
        registerOutgoingPluginChannel(plugin, channel);
    }

    @Override
    public void unregisterIncomingPluginChannel(Plugin plugin, String channel) {
        unregisterOutgoingPluginChannel(plugin, channel);
    }

    @Override
    public void unregisterIncomingPluginChannel(Plugin plugin) {
        unregisterOutgoingPluginChannel(plugin);
    }
}
