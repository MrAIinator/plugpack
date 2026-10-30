package org.bukkit.plugin;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class PluginBase implements Plugin {
    @Override
    public final int hashCode() {
        return getName().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Plugin other)) return false;
        return getName().equals(other.getName());
    }

    @Override
    public final String toString() {
        return getDescription().getFullName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
