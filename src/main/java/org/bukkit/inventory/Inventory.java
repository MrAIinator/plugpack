package org.bukkit.inventory;

import java.util.HashMap;

public interface Inventory {
    int getSize();

    ItemStack getItem(int index);

    void setItem(int index, ItemStack item);

    HashMap<Integer, ItemStack> addItem(ItemStack... items);

    void clear();
}
