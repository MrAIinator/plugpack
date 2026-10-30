package org.bukkit.command;

import java.util.List;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginCommand extends Command {
    private final JavaPlugin owner;
    private CommandExecutor executor;
    private TabCompleter tabCompleter;

    public PluginCommand(String name, JavaPlugin owner) {
        super(name);
        this.owner = owner;
        this.executor = owner;
        this.tabCompleter = owner;
    }

    public JavaPlugin getPlugin() {
        return owner;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor == null ? owner : executor;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public void setTabCompleter(TabCompleter completer) {
        this.tabCompleter = completer;
    }

    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!owner.isEnabled()) return false;
        if (!testPermission(sender)) return true;
        try {
            boolean success = executor.onCommand(sender, this, commandLabel, args);
            if (!success && !getUsage().isEmpty()) sender.sendMessage(getUsage());
            return success;
        } catch (Throwable thrown) {
            owner.getLogger().log(Level.SEVERE, "Unhandled exception executing '" + commandLabel + "' in " + owner.getName(), thrown);
            sender.sendMessage("\u00A7cAn internal error occurred while attempting to perform this command.");
            return false;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!testPermissionSilent(sender)) return super.tabComplete(sender, alias, args);
        try {
            if (tabCompleter != null) {
                List<String> result = tabCompleter.onTabComplete(sender, this, alias, args);
                if (result != null) return result;
            }
        } catch (Throwable thrown) {
            owner.getLogger().log(Level.SEVERE, "Unhandled exception during tab complete for '" + alias + "'", thrown);
        }
        return super.tabComplete(sender, alias, args);
    }
}
