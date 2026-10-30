package dev.mrainator.plugpack.bridge;

import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;

public final class McItems {
    private McItems() {
    }

    public static net.minecraft.world.item.ItemStack toMc(org.bukkit.inventory.ItemStack stack) {
        if (stack == null || stack.getType() == null || stack.getType().isAir() || stack.getAmount() <= 0) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.parse("minecraft:" + stack.getType().getKey());
        net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(id);
        return new net.minecraft.world.item.ItemStack(item, stack.getAmount());
    }

    public static org.bukkit.inventory.ItemStack fromMc(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) return new org.bukkit.inventory.ItemStack(Material.AIR);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Material material = id == null ? Material.AIR : Material.matchMaterial(id.getPath());
        if (material == null) material = Material.AIR;
        return new org.bukkit.inventory.ItemStack(material, stack.getCount());
    }

    public static Material materialOf(BlockState state) {
        if (state == null) return Material.AIR;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null) return Material.AIR;
        Material material = Material.matchMaterial(id.getPath());
        return material == null ? Material.AIR : material;
    }

    public static BlockState stateOf(Material material) {
        if (material == null || material.isAir()) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        ResourceLocation id = ResourceLocation.parse("minecraft:" + material.getKey().toLowerCase(Locale.ROOT));
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block.defaultBlockState();
    }
}
