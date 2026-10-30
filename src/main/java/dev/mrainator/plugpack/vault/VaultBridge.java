package dev.mrainator.plugpack.vault;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import dev.mrainator.plugpack.load.SimplePluginManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.ServicePriority;

public final class VaultBridge {
    private VaultBridge() {
    }

    public static void install(SimplePluginManager manager) {
        BuiltinPlugin plugin = new BuiltinPlugin();
        manager.add(plugin);
        PlugPermission permissions = new PlugPermission();
        manager.register(Permission.class, permissions, plugin, ServicePriority.Normal);
        manager.register(Economy.class, new NoEconomy(), plugin, ServicePriority.Lowest);
        manager.register(Chat.class, new EmptyChat(permissions), plugin, ServicePriority.Lowest);
    }

    private static final class PlugPermission extends Permission {
        @Override
        public String getName() {
            return "PlugPack";
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean hasSuperPermsCompat() {
            return true;
        }

        @Override
        public boolean hasGroupSupport() {
            return false;
        }

        @Override
        public boolean playerHas(String world, String player, String permission) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(player);
            if (offline instanceof Player online) return online.hasPermission(permission);
            return offline.isOp();
        }

        @Override
        public boolean playerAdd(String world, String player, String permission) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(player);
            ((dev.mrainator.plugpack.bridge.NeoServer) Bukkit.getServer()).permissions().set(offline.getUniqueId(), permission, true);
            return true;
        }

        @Override
        public boolean playerRemove(String world, String player, String permission) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(player);
            ((dev.mrainator.plugpack.bridge.NeoServer) Bukkit.getServer()).permissions().set(offline.getUniqueId(), permission, false);
            return true;
        }

        @Override
        public boolean groupHas(String world, String group, String permission) {
            return false;
        }

        @Override
        public boolean groupAdd(String world, String group, String permission) {
            return false;
        }

        @Override
        public boolean groupRemove(String world, String group, String permission) {
            return false;
        }

        @Override
        public boolean playerInGroup(String world, String player, String group) {
            return false;
        }

        @Override
        public boolean playerAddGroup(String world, String player, String group) {
            return false;
        }

        @Override
        public boolean playerRemoveGroup(String world, String player, String group) {
            return false;
        }

        @Override
        public String[] getPlayerGroups(String world, String player) {
            return new String[0];
        }

        @Override
        public String getPrimaryGroup(String world, String player) {
            return "";
        }

        @Override
        public String[] getGroups() {
            return new String[0];
        }
    }

    private static final class NoEconomy extends Economy {
        private EconomyResponse fail() {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "PlugPack has no economy. Add an economy plugin.");
        }

        @Override public boolean isEnabled() { return false; }
        @Override public String getName() { return "PlugPack-None"; }
        @Override public int fractionalDigits() { return 2; }
        @Override public String format(double amount) { return String.valueOf(amount); }
        @Override public String currencyNamePlural() { return "coins"; }
        @Override public String currencyNameSingular() { return "coin"; }
        @Override public boolean hasAccount(String playerName) { return false; }
        @Override public boolean hasAccount(String playerName, String worldName) { return false; }
        @Override public double getBalance(String playerName) { return 0; }
        @Override public double getBalance(String playerName, String world) { return 0; }
        @Override public boolean has(String playerName, double amount) { return false; }
        @Override public boolean has(String playerName, String worldName, double amount) { return false; }
        @Override public EconomyResponse withdrawPlayer(String playerName, double amount) { return fail(); }
        @Override public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) { return fail(); }
        @Override public EconomyResponse depositPlayer(String playerName, double amount) { return fail(); }
        @Override public EconomyResponse depositPlayer(String playerName, String worldName, double amount) { return fail(); }
        @Override public boolean createPlayerAccount(String playerName) { return false; }
        @Override public boolean createPlayerAccount(String playerName, String worldName) { return false; }
    }

    private static final class EmptyChat extends Chat {
        EmptyChat(Permission perms) {
            super(perms);
        }

        @Override public String getName() { return "PlugPack"; }
        @Override public boolean isEnabled() { return true; }
        @Override public String getPlayerPrefix(String world, String player) { return ""; }
        @Override public String getPlayerSuffix(String world, String player) { return ""; }
        @Override public void setPlayerPrefix(String world, String player, String prefix) {}
        @Override public void setPlayerSuffix(String world, String player, String suffix) {}
        @Override public String getGroupPrefix(String world, String group) { return ""; }
        @Override public String getGroupSuffix(String world, String group) { return ""; }
        @Override public void setGroupPrefix(String world, String group, String prefix) {}
        @Override public void setGroupSuffix(String world, String group, String suffix) {}
    }

    private static final class BuiltinPlugin implements Plugin {
        private final PluginDescriptionFile description = new PluginDescriptionFile("Vault", "1.7.1-plugpack", "builtin");
        private final File folder = new File("plugins/Vault");
        private final Logger logger = Logger.getLogger("PlugPack/Vault");

        @Override public File getDataFolder() { return folder; }
        @Override public PluginDescriptionFile getDescription() { return description; }
        @Override public FileConfiguration getConfig() { return new YamlConfiguration(); }
        @Override public InputStream getResource(String filename) { return null; }
        @Override public void saveConfig() {}
        @Override public void saveDefaultConfig() {}
        @Override public void saveResource(String resourcePath, boolean replace) {}
        @Override public void reloadConfig() {}
        @Override public PluginLoader getPluginLoader() { return Bukkit.getPluginManager() instanceof PluginLoader loader ? loader : null; }
        @Override public Server getServer() { return Bukkit.getServer(); }
        @Override public boolean isEnabled() { return true; }
        @Override public void onDisable() {}
        @Override public void onLoad() {}
        @Override public void onEnable() {}
        @Override public boolean isNaggable() { return false; }
        @Override public void setNaggable(boolean canNag) {}
        @Override public Logger getLogger() { return logger; }
        @Override public String getName() { return "Vault"; }
        @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { return false; }
        @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { return null; }
        @Override public int hashCode() { return getName().hashCode(); }
        @Override public boolean equals(Object obj) { return obj instanceof Plugin other && getName().equals(other.getName()); }
        @Override public String toString() { return description.getFullName(); }
    }
}
