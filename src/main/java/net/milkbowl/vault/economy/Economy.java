package net.milkbowl.vault.economy;

import org.bukkit.OfflinePlayer;

public abstract class Economy {
    public abstract boolean isEnabled();

    public abstract String getName();

    public abstract int fractionalDigits();

    public abstract String format(double amount);

    public abstract String currencyNamePlural();

    public abstract String currencyNameSingular();

    public abstract boolean hasAccount(String playerName);

    public abstract boolean hasAccount(String playerName, String worldName);

    public abstract double getBalance(String playerName);

    public abstract double getBalance(String playerName, String world);

    public abstract boolean has(String playerName, double amount);

    public abstract boolean has(String playerName, String worldName, double amount);

    public abstract EconomyResponse withdrawPlayer(String playerName, double amount);

    public abstract EconomyResponse withdrawPlayer(String playerName, String worldName, double amount);

    public abstract EconomyResponse depositPlayer(String playerName, double amount);

    public abstract EconomyResponse depositPlayer(String playerName, String worldName, double amount);

    public abstract boolean createPlayerAccount(String playerName);

    public abstract boolean createPlayerAccount(String playerName, String worldName);

    public boolean hasAccount(OfflinePlayer player) {
        return hasAccount(player.getName());
    }

    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player.getName(), worldName);
    }

    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getName());
    }

    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player.getName(), world);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return has(player.getName(), amount);
    }

    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player.getName(), worldName, amount);
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player.getName(), amount);
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player.getName(), worldName, amount);
    }

    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return depositPlayer(player.getName(), amount);
    }

    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player.getName(), worldName, amount);
    }

    public boolean createPlayerAccount(OfflinePlayer player) {
        return createPlayerAccount(player.getName());
    }

    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player.getName(), worldName);
    }

    public String format(double amount, String world) {
        return format(amount);
    }
}
