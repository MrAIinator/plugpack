package org.bukkit.inventory;

import org.bukkit.Material;

public class ItemStack implements Cloneable {
    private Material type;
    private int amount;

    public ItemStack(Material type) {
        this(type, 1);
    }

    public ItemStack(Material type, int amount) {
        this.type = type == null ? Material.AIR : type;
        this.amount = Math.max(0, amount);
    }

    public Material getType() {
        return type;
    }

    public void setType(Material type) {
        this.type = type == null ? Material.AIR : type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
        if (this.amount == 0) this.type = Material.AIR;
    }

    public int getMaxStackSize() {
        return type.getMaxStackSize();
    }

    public boolean isSimilar(ItemStack other) {
        return other != null && other.type == type;
    }

    @Override
    public ItemStack clone() {
        return new ItemStack(type, amount);
    }

    @Override
    public String toString() {
        return "ItemStack{" + type + " x " + amount + "}";
    }
}
