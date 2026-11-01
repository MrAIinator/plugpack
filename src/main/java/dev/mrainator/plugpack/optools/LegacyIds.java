package dev.mrainator.plugpack.optools;

import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** Classic numeric block ids, the ones old WorldEdit took in //set. */
final class LegacyIds {
    private static final String[] IDS = new String[176];
    private static final String[] WOOL = {
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
    };
    private static final String[] LOGS = {"oak", "spruce", "birch", "jungle"};

    static {
        put(0, "air");
        put(1, "stone");
        put(2, "grass_block");
        put(3, "dirt");
        put(4, "cobblestone");
        put(5, "oak_planks");
        put(6, "oak_sapling");
        put(7, "bedrock");
        put(8, "water");
        put(9, "water");
        put(10, "lava");
        put(11, "lava");
        put(12, "sand");
        put(13, "gravel");
        put(14, "gold_ore");
        put(15, "iron_ore");
        put(16, "coal_ore");
        put(17, "oak_log");
        put(18, "oak_leaves");
        put(19, "sponge");
        put(20, "glass");
        put(21, "lapis_ore");
        put(22, "lapis_block");
        put(23, "dispenser");
        put(24, "sandstone");
        put(25, "note_block");
        put(26, "red_bed");
        put(27, "powered_rail");
        put(28, "detector_rail");
        put(29, "sticky_piston");
        put(30, "cobweb");
        put(31, "short_grass");
        put(32, "dead_bush");
        put(33, "piston");
        put(34, "piston_head");
        put(35, "white_wool");
        put(36, "piston_head");
        put(37, "dandelion");
        put(38, "poppy");
        put(39, "brown_mushroom");
        put(40, "red_mushroom");
        put(41, "gold_block");
        put(42, "iron_block");
        put(43, "smooth_stone");
        put(44, "stone_slab");
        put(45, "bricks");
        put(46, "tnt");
        put(47, "bookshelf");
        put(48, "mossy_cobblestone");
        put(49, "obsidian");
        put(50, "torch");
        put(51, "fire");
        put(52, "spawner");
        put(53, "oak_stairs");
        put(54, "chest");
        put(55, "redstone_wire");
        put(56, "diamond_ore");
        put(57, "diamond_block");
        put(58, "crafting_table");
        put(59, "wheat");
        put(60, "farmland");
        put(61, "furnace");
        put(62, "furnace");
        put(63, "oak_sign");
        put(64, "oak_door");
        put(65, "ladder");
        put(66, "rail");
        put(67, "cobblestone_stairs");
        put(68, "oak_wall_sign");
        put(69, "lever");
        put(70, "stone_pressure_plate");
        put(71, "iron_door");
        put(72, "oak_pressure_plate");
        put(73, "redstone_ore");
        put(74, "redstone_ore");
        put(75, "redstone_torch");
        put(76, "redstone_torch");
        put(77, "stone_button");
        put(78, "snow");
        put(79, "ice");
        put(80, "snow_block");
        put(81, "cactus");
        put(82, "clay");
        put(83, "sugar_cane");
        put(84, "jukebox");
        put(85, "oak_fence");
        put(86, "pumpkin");
        put(87, "netherrack");
        put(88, "soul_sand");
        put(89, "glowstone");
        put(90, "nether_portal");
        put(91, "jack_o_lantern");
        put(92, "cake");
        put(93, "repeater");
        put(94, "repeater");
        put(95, "white_stained_glass");
        put(96, "oak_trapdoor");
        put(97, "infested_stone");
        put(98, "stone_bricks");
        put(99, "brown_mushroom_block");
        put(100, "red_mushroom_block");
        put(101, "iron_bars");
        put(102, "glass_pane");
        put(103, "melon");
        put(104, "pumpkin_stem");
        put(105, "melon_stem");
        put(106, "vine");
        put(107, "oak_fence_gate");
        put(108, "brick_stairs");
        put(109, "stone_brick_stairs");
        put(110, "mycelium");
        put(111, "lily_pad");
        put(112, "nether_bricks");
        put(113, "nether_brick_fence");
        put(114, "nether_brick_stairs");
        put(115, "nether_wart");
        put(116, "enchanting_table");
        put(117, "brewing_stand");
        put(118, "cauldron");
        put(119, "end_portal");
        put(120, "end_portal_frame");
        put(121, "end_stone");
        put(122, "dragon_egg");
        put(123, "redstone_lamp");
        put(124, "redstone_lamp");
        put(125, "oak_planks");
        put(126, "oak_slab");
        put(127, "cocoa");
        put(128, "sandstone_stairs");
        put(129, "emerald_ore");
        put(130, "ender_chest");
        put(131, "tripwire_hook");
        put(132, "tripwire");
        put(133, "emerald_block");
        put(134, "spruce_stairs");
        put(135, "birch_stairs");
        put(136, "jungle_stairs");
        put(137, "command_block");
        put(138, "beacon");
        put(139, "cobblestone_wall");
        put(140, "flower_pot");
        put(141, "carrots");
        put(142, "potatoes");
        put(143, "oak_button");
        put(144, "skeleton_skull");
        put(145, "anvil");
        put(146, "trapped_chest");
        put(147, "light_weighted_pressure_plate");
        put(148, "heavy_weighted_pressure_plate");
        put(149, "comparator");
        put(150, "comparator");
        put(151, "daylight_detector");
        put(152, "redstone_block");
        put(153, "nether_quartz_ore");
        put(154, "hopper");
        put(155, "quartz_block");
        put(156, "quartz_stairs");
        put(157, "activator_rail");
        put(158, "dropper");
        put(159, "white_terracotta");
        put(160, "white_stained_glass_pane");
        put(161, "acacia_leaves");
        put(162, "acacia_log");
        put(163, "acacia_stairs");
        put(164, "dark_oak_stairs");
        put(165, "slime_block");
        put(166, "barrier");
        put(167, "iron_trapdoor");
        put(168, "prismarine");
        put(169, "sea_lantern");
        put(170, "hay_block");
        put(171, "white_carpet");
        put(172, "terracotta");
        put(173, "coal_block");
        put(174, "packed_ice");
        put(175, "sunflower");
    }

    private LegacyIds() {
    }

    static Block parse(String raw) {
        if (raw == null) return null;
        String text = raw.trim();
        int colon = text.indexOf(':');
        String idPart = colon < 0 ? text : text.substring(0, colon);
        String dataPart = colon < 0 ? "" : text.substring(colon + 1);
        int id;
        try {
            id = Integer.parseInt(idPart);
        } catch (NumberFormatException ignored) {
            return null;
        }
        int data = 0;
        if (!dataPart.isEmpty()) {
            try {
                data = Integer.parseInt(dataPart);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        String name = null;
        if (id == 35 && data >= 0 && data < WOOL.length) name = WOOL[data] + "_wool";
        else if (id == 17 && data >= 0 && data < LOGS.length) name = LOGS[data] + "_log";
        else if (id == 18 && data >= 0 && data < LOGS.length) name = LOGS[data] + "_leaves";
        else if (id >= 0 && id < IDS.length) name = IDS[id];
        if (name == null) return null;
        ResourceLocation key = ResourceLocation.tryParse("minecraft:" + name);
        return key == null ? null : BuiltInRegistries.BLOCK.getOptional(key).orElse(null);
    }

    static String catalog(int from) {
        int start = Math.max(0, from);
        int end = Math.min(IDS.length, start + 24);
        StringBuilder out = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (IDS[i] == null) continue;
            if (!out.isEmpty()) out.append(", ");
            out.append(i).append(' ').append(label(IDS[i]));
        }
        if (end < IDS.length) out.append(". Дальше: //ids ").append(end);
        return out.toString();
    }

    private static String label(String name) {
        return switch (name) {
            case "air" -> "воздух";
            case "stone" -> "камень";
            case "grass_block" -> "трава";
            case "dirt" -> "земля";
            case "cobblestone" -> "булыжник";
            case "oak_planks" -> "доски";
            case "oak_sapling" -> "саженец";
            case "bedrock" -> "бедрок";
            case "water" -> "вода";
            case "lava" -> "лава";
            case "sand" -> "песок";
            case "gravel" -> "гравий";
            case "gold_ore" -> "золотая руда";
            case "iron_ore" -> "железная руда";
            case "coal_ore" -> "угольная руда";
            case "oak_log" -> "бревно";
            case "oak_leaves" -> "листва";
            case "sponge" -> "губка";
            case "glass" -> "стекло";
            case "lapis_ore" -> "лазуритовая руда";
            case "lapis_block" -> "лазурит";
            case "dispenser" -> "раздатчик";
            default -> name.toLowerCase(Locale.ROOT);
        };
    }

    private static void put(int id, String name) {
        IDS[id] = name;
    }
}
