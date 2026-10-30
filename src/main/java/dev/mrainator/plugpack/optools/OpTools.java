package dev.mrainator.plugpack.optools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mrainator.plugpack.bridge.McItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLPaths;
import org.bukkit.Material;

/**
 * Operator-only cuboid edit and region lock.
 * Official WorldEdit and WorldGuard jars still need Paper/NMS and will not load.
 */
public final class OpTools {
    private static final int MAX_VOLUME = 32768;
    private static final Map<UUID, Selection> selections = new HashMap<>();
    private static final Map<UUID, Deque<List<Change>>> undos = new HashMap<>();
    private static final Map<UUID, Clipboard> clips = new HashMap<>();
    private static final List<Region> regions = new ArrayList<>();
    private static Path regionFile;

    private OpTools() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        regionFile = FMLPaths.GAMEDIR.get().resolve("config").resolve("plugpack").resolve("regions.txt");
        load();
        if (dispatcher.getRoot().getChild("/") == null) {
            dispatcher.register(Commands.literal("/")
                    .requires(OpTools::op)
                    .then(Commands.literal("wand").executes(ctx -> wand(player(ctx.getSource()))))
                    .then(Commands.literal("pos1").executes(ctx -> pos(player(ctx.getSource()), true)))
                    .then(Commands.literal("pos2").executes(ctx -> pos(player(ctx.getSource()), false)))
                    .then(Commands.literal("set").then(Commands.argument("block", StringArgumentType.word())
                            .executes(ctx -> set(player(ctx.getSource()), StringArgumentType.getString(ctx, "block"), null))))
                    .then(Commands.literal("replace")
                            .then(Commands.argument("from", StringArgumentType.word())
                                    .then(Commands.argument("to", StringArgumentType.word())
                                            .executes(ctx -> set(player(ctx.getSource()),
                                                    StringArgumentType.getString(ctx, "to"),
                                                    StringArgumentType.getString(ctx, "from"))))))
                    .then(Commands.literal("undo").executes(ctx -> undo(player(ctx.getSource()))))
                    .then(Commands.literal("copy").executes(ctx -> copy(player(ctx.getSource()))))
                    .then(Commands.literal("paste").executes(ctx -> paste(player(ctx.getSource())))));
        }
        var region = Commands.literal("region")
                .requires(OpTools::op)
                .executes(ctx -> list(player(ctx.getSource())))
                .then(Commands.literal("list").executes(ctx -> list(player(ctx.getSource()))))
                .then(Commands.literal("define").then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> define(player(ctx.getSource()), StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> remove(player(ctx.getSource()), StringArgumentType.getString(ctx, "name")))));
        if (dispatcher.getRoot().getChild("region") == null) {
            var regionNode = dispatcher.register(region);
            if (dispatcher.getRoot().getChild("rg") == null) {
                dispatcher.register(Commands.literal("rg").redirect(regionNode));
            }
        }
    }

    public static boolean onAxe(ServerPlayer player, BlockPos pos, boolean left) {
        if (player == null || pos == null || !player.hasPermissions(2)) return false;
        if (!player.getMainHandItem().is(Items.WOODEN_AXE)) return false;
        mark(player, pos.immutable(), left);
        tell(player, (left ? "Позиция 1" : "Позиция 2") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
        return true;
    }

    public static boolean blocked(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (player == null || level == null || pos == null || player.hasPermissions(2)) return false;
        String dim = level.dimension().location().toString();
        for (Region region : regions) {
            if (region.dim.equals(dim) && region.contains(pos)) return true;
        }
        return false;
    }

    private static boolean op(CommandSourceStack source) {
        return source.hasPermission(2);
    }

    private static ServerPlayer player(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) return player;
        source.sendFailure(net.minecraft.network.chat.Component.literal("Только игрок-оператор."));
        return null;
    }

    private static int wand(ServerPlayer player) {
        if (player == null) return 0;
        player.getInventory().add(new ItemStack(Items.WOODEN_AXE));
        tell(player, "Топор оператора. ЛКМ — позиция 1, ПКМ — позиция 2.");
        return 1;
    }

    private static int pos(ServerPlayer player, boolean first) {
        if (player == null) return 0;
        mark(player, player.blockPosition(), first);
        tell(player, (first ? "Позиция 1" : "Позиция 2") + " по ногам.");
        return 1;
    }

    private static void mark(ServerPlayer player, BlockPos pos, boolean first) {
        Selection selection = selections.computeIfAbsent(player.getUUID(), id -> new Selection());
        selection.dim = player.serverLevel().dimension().location().toString();
        if (first) selection.a = pos.immutable();
        else selection.b = pos.immutable();
    }

    private static int set(ServerPlayer player, String toName, String fromName) {
        if (player == null) return 0;
        Selection selection = ready(player);
        if (selection == null) return 0;
        Material to = material(toName);
        Material from = fromName == null ? null : material(fromName);
        if (to == null || !to.isBlock() || (fromName != null && (from == null || !from.isBlock()))) {
            tell(player, "Нужен id блока, например stone или oak_planks.");
            return 0;
        }
        BlockState state = McItems.stateOf(to);
        List<Change> changes = new ArrayList<>();
        int count = 0;
        ServerLevel level = player.serverLevel();
        for (BlockPos pos : BlockPos.betweenClosed(selection.min(), selection.max())) {
            BlockState old = level.getBlockState(pos);
            if (from != null && McItems.materialOf(old) != from) continue;
            if (old == state) continue;
            changes.add(new Change(pos.immutable(), old));
            level.setBlockAndUpdate(pos, state);
            count++;
        }
        push(player.getUUID(), changes);
        tell(player, "Блоков: " + count);
        return 1;
    }

    private static int undo(ServerPlayer player) {
        if (player == null) return 0;
        Deque<List<Change>> stack = undos.get(player.getUUID());
        if (stack == null || stack.isEmpty()) {
            tell(player, "Откатывать нечего.");
            return 0;
        }
        ServerLevel level = player.serverLevel();
        for (Change change : stack.pop()) level.setBlockAndUpdate(change.pos, change.state);
        tell(player, "Откат сделан.");
        return 1;
    }

    private static int copy(ServerPlayer player) {
        if (player == null) return 0;
        Selection selection = ready(player);
        if (selection == null) return 0;
        BlockPos origin = player.blockPosition();
        List<Rel> blocks = new ArrayList<>();
        ServerLevel level = player.serverLevel();
        for (BlockPos pos : BlockPos.betweenClosed(selection.min(), selection.max())) {
            blocks.add(new Rel(pos.getX() - origin.getX(), pos.getY() - origin.getY(), pos.getZ() - origin.getZ(), level.getBlockState(pos)));
        }
        clips.put(player.getUUID(), new Clipboard(blocks));
        tell(player, "Скопировано блоков: " + blocks.size());
        return 1;
    }

    private static int paste(ServerPlayer player) {
        if (player == null) return 0;
        Clipboard clip = clips.get(player.getUUID());
        if (clip == null || clip.blocks.isEmpty()) {
            tell(player, "Буфер пуст. Сначала //copy.");
            return 0;
        }
        BlockPos origin = player.blockPosition();
        ServerLevel level = player.serverLevel();
        List<Change> changes = new ArrayList<>();
        for (Rel rel : clip.blocks) {
            BlockPos pos = origin.offset(rel.dx, rel.dy, rel.dz);
            changes.add(new Change(pos, level.getBlockState(pos)));
            level.setBlockAndUpdate(pos, rel.state);
        }
        push(player.getUUID(), changes);
        tell(player, "Вставлено блоков: " + clip.blocks.size());
        return 1;
    }

    private static int define(ServerPlayer player, String name) {
        if (player == null) return 0;
        Selection selection = ready(player);
        if (selection == null) return 0;
        String id = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
        if (id.isEmpty()) {
            tell(player, "Имя региона: латиница и цифры.");
            return 0;
        }
        regions.removeIf(region -> region.name.equals(id));
        BlockPos min = selection.min();
        BlockPos max = selection.max();
        regions.add(new Region(id, selection.dim, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()));
        save();
        tell(player, "Регион " + id + " закрыт для всех, кроме операторов.");
        return 1;
    }

    private static int remove(ServerPlayer player, String name) {
        if (player == null) return 0;
        String id = name.toLowerCase(Locale.ROOT);
        boolean removed = regions.removeIf(region -> region.name.equals(id));
        save();
        tell(player, removed ? "Регион снят." : "Такого региона нет.");
        return removed ? 1 : 0;
    }

    private static int list(ServerPlayer player) {
        if (player == null) return 0;
        if (regions.isEmpty()) {
            tell(player, "Регионов нет.");
            return 1;
        }
        StringBuilder out = new StringBuilder("Регионы: ");
        for (int i = 0; i < regions.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(regions.get(i).name);
        }
        tell(player, out.toString());
        return 1;
    }

    private static Selection ready(ServerPlayer player) {
        Selection selection = selections.get(player.getUUID());
        if (selection == null || selection.a == null || selection.b == null) {
            tell(player, "Нет выделения. //wand или //pos1 и //pos2.");
            return null;
        }
        if (!selection.dim.equals(player.serverLevel().dimension().location().toString())) {
            tell(player, "Выделение в другом мире.");
            return null;
        }
        long volume = selection.volume();
        if (volume <= 0 || volume > MAX_VOLUME) {
            tell(player, "Слишком большой куб. Лимит " + MAX_VOLUME + " блоков.");
            return null;
        }
        return selection;
    }

    private static Material material(String name) {
        if (name == null) return null;
        return Material.matchMaterial(name.toLowerCase(Locale.ROOT).replace("minecraft:", ""));
    }

    private static void push(UUID id, List<Change> changes) {
        if (changes.isEmpty()) return;
        Deque<List<Change>> stack = undos.computeIfAbsent(id, ignored -> new ArrayDeque<>());
        stack.push(changes);
        while (stack.size() > 8) stack.removeLast();
    }

    private static void load() {
        regions.clear();
        if (regionFile == null || !Files.isRegularFile(regionFile)) return;
        try {
            for (String line : Files.readAllLines(regionFile, StandardCharsets.UTF_8)) {
                String[] parts = line.trim().split(",");
                if (parts.length != 8) continue;
                regions.add(new Region(parts[0], parts[1],
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), Integer.parseInt(parts[7])));
            }
        } catch (Exception ignored) {
            regions.clear();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(regionFile.getParent());
            List<String> lines = new ArrayList<>();
            for (Region region : regions) {
                lines.add(region.name + "," + region.dim + "," + region.x1 + "," + region.y1 + "," + region.z1
                        + "," + region.x2 + "," + region.y2 + "," + region.z2);
            }
            Files.write(regionFile, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static void tell(ServerPlayer player, String message) {
        player.sendSystemMessage(dev.mrainator.plugpack.bridge.LegacyText.toComponent(message));
    }

    private static final class Selection {
        String dim;
        BlockPos a;
        BlockPos b;

        BlockPos min() {
            return new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
        }

        BlockPos max() {
            return new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
        }

        long volume() {
            BlockPos min = min();
            BlockPos max = max();
            return (long) (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
        }
    }

    private record Change(BlockPos pos, BlockState state) {
    }

    private record Rel(int dx, int dy, int dz, BlockState state) {
    }

    private record Clipboard(List<Rel> blocks) {
    }

    private static final class Region {
        final String name;
        final String dim;
        final int x1, y1, z1, x2, y2, z2;

        Region(String name, String dim, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.name = name;
            this.dim = dim;
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.z1 = Math.min(z1, z2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.z2 = Math.max(z1, z2);
        }

        boolean contains(BlockPos pos) {
            return pos.getX() >= x1 && pos.getX() <= x2
                    && pos.getY() >= y1 && pos.getY() <= y2
                    && pos.getZ() >= z1 && pos.getZ() <= z2;
        }
    }
}
