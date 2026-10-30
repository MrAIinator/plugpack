package org.bukkit.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Command {
    private final String name;
    private String label;
    private String description = "";
    private String usageMessage = "";
    private String permission;
    private String permissionMessage = "\u00A7cI'm sorry, but you do not have permission to perform this command.";
    private List<String> aliases = new ArrayList<>();

    protected Command(String name) {
        this.name = name;
        this.label = name;
    }

    public abstract boolean execute(CommandSender sender, String commandLabel, String[] args);

    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public boolean setLabel(String name) {
        this.label = name;
        return true;
    }

    public String getDescription() {
        return description;
    }

    public Command setDescription(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    public String getUsage() {
        return usageMessage;
    }

    public Command setUsage(String usage) {
        this.usageMessage = usage == null ? "" : usage;
        return this;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public Command setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
        return this;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public Command setAliases(List<String> aliases) {
        this.aliases = aliases == null ? new ArrayList<>() : new ArrayList<>(aliases);
        return this;
    }

    public boolean testPermission(CommandSender target) {
        if (testPermissionSilent(target)) return true;
        if (permissionMessage != null && !permissionMessage.isEmpty()) target.sendMessage(permissionMessage);
        return false;
    }

    public boolean testPermissionSilent(CommandSender target) {
        return permission == null || permission.isEmpty() || target.hasPermission(permission);
    }
}
