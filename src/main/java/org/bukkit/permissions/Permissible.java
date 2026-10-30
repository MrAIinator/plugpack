package org.bukkit.permissions;

public interface Permissible {
    boolean isPermissionSet(String name);

    boolean hasPermission(String name);

    boolean isOp();

    void setOp(boolean value);
}
