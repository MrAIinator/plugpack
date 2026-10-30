package dev.mrainator.plugpack.bridge;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

final class NeoConsole implements ConsoleCommandSender {
    private final NeoServer host;

    NeoConsole(NeoServer host) {
        this.host = host;
    }

    @Override
    public void sendMessage(String message) {
        dev.mrainator.plugpack.PlugPack.LOGGER.info("[console] {}", LegacyText.plain(message));
    }

    @Override
    public void sendMessage(String[] messages) {
        if (messages == null) return;
        for (String message : messages) sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent component) {
        sendMessage(component == null ? "" : component.toLegacyText());
    }

    @Override
    public void sendMessage(BaseComponent... components) {
        sendMessage(BaseComponent.toLegacyText(components));
    }

    @Override
    public Server getServer() {
        return host;
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public boolean isPermissionSet(String name) {
        return true;
    }

    @Override
    public boolean hasPermission(String name) {
        return true;
    }

    @Override
    public Spigot spigot() {
        return new Spigot() {
            @Override
            public void sendMessage(BaseComponent component) {
                NeoConsole.this.sendMessage(component);
            }

            @Override
            public void sendMessage(BaseComponent... components) {
                NeoConsole.this.sendMessage(components);
            }
        };
    }
}

class NeoOffline implements OfflinePlayer {
    final NeoServer host;
    final UUID id;
    String name;

    NeoOffline(NeoServer host, UUID id, String name) {
        this.host = host;
        this.id = id;
        this.name = name;
    }

    @Override
    public UUID getUniqueId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOnline() {
        return host.minecraft().getPlayerList().getPlayer(id) != null;
    }

    @Override
    public boolean isOp() {
        return host.minecraft().getPlayerList().isOp(new GameProfile(id, name == null ? "" : name));
    }

    @Override
    public void setOp(boolean value) {
        GameProfile profile = new GameProfile(id, name == null ? "" : name);
        if (value) host.minecraft().getPlayerList().op(profile);
        else host.minecraft().getPlayerList().deop(profile);
    }

    @Override
    public Player getPlayer() {
        ServerPlayer player = host.minecraft().getPlayerList().getPlayer(id);
        return player == null ? null : host.wrap(player);
    }

    @Override
    public boolean hasPlayedBefore() {
        return isOnline() || isOp();
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }
}

final class NeoPlayer extends NeoOffline implements Player {
    private String displayName;
    private final NeoInventory inventory = new NeoInventory(this);

    NeoPlayer(NeoServer host, UUID id, String name) {
        super(host, id, name);
        this.displayName = name;
    }

    ServerPlayer online() {
        ServerPlayer player = host.minecraft().getPlayerList().getPlayer(id);
        if (player != null) name = player.getGameProfile().getName();
        return player;
    }

    @Override
    public void sendMessage(String message) {
        ServerPlayer player = online();
        if (player != null) player.sendSystemMessage(LegacyText.toComponent(message));
    }

    @Override
    public void sendMessage(String[] messages) {
        if (messages == null) return;
        for (String message : messages) sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent component) {
        sendMessage(component == null ? "" : component.toLegacyText());
    }

    @Override
    public void sendMessage(BaseComponent... components) {
        sendMessage(BaseComponent.toLegacyText(components));
    }

    @Override
    public Server getServer() {
        return host;
    }

    @Override
    public boolean isPermissionSet(String node) {
        return host.permissions().isSet(id, node, isOp());
    }

    @Override
    public boolean hasPermission(String node) {
        return host.permissions().has(id, node, isOp());
    }

    @Override
    public String getDisplayName() {
        return displayName == null ? name : displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public GameMode getGameMode() {
        ServerPlayer player = online();
        if (player == null) return GameMode.SURVIVAL;
        return switch (player.gameMode.getGameModeForPlayer()) {
            case CREATIVE -> GameMode.CREATIVE;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
            default -> GameMode.SURVIVAL;
        };
    }

    @Override
    public void setGameMode(GameMode mode) {
        ServerPlayer player = online();
        if (player == null || mode == null) return;
        player.setGameMode(switch (mode) {
            case CREATIVE -> GameType.CREATIVE;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
            case SURVIVAL -> GameType.SURVIVAL;
        });
    }

    @Override
    public double getHealth() {
        ServerPlayer player = online();
        return player == null ? 0 : player.getHealth();
    }

    @Override
    public void setHealth(double health) {
        ServerPlayer player = online();
        if (player != null) player.setHealth((float) health);
    }

    @Override
    public double getMaxHealth() {
        ServerPlayer player = online();
        return player == null ? 20 : player.getMaxHealth();
    }

    @Override
    public int getFoodLevel() {
        ServerPlayer player = online();
        return player == null ? 0 : player.getFoodData().getFoodLevel();
    }

    @Override
    public void setFoodLevel(int level) {
        ServerPlayer player = online();
        if (player != null) player.getFoodData().setFoodLevel(level);
    }

    @Override
    public int getLevel() {
        ServerPlayer player = online();
        return player == null ? 0 : player.experienceLevel;
    }

    @Override
    public void setLevel(int level) {
        ServerPlayer player = online();
        if (player != null) player.experienceLevel = level;
    }

    @Override
    public float getExp() {
        ServerPlayer player = online();
        return player == null ? 0 : player.experienceProgress;
    }

    @Override
    public void setExp(float exp) {
        ServerPlayer player = online();
        if (player != null) player.experienceProgress = exp;
    }

    @Override
    public void giveExp(int amount) {
        ServerPlayer player = online();
        if (player != null) player.giveExperiencePoints(amount);
    }

    @Override
    public PlayerInventory getInventory() {
        return inventory;
    }

    @Override
    public ItemStack getItemInHand() {
        return inventory.getItemInMainHand();
    }

    @Override
    public void kickPlayer(String reason) {
        ServerPlayer player = online();
        if (player != null) player.connection.disconnect(LegacyText.toComponent(reason == null ? "Kicked" : reason));
    }

    @Override
    public void chat(String message) {
        if (message == null) return;
        if (message.startsWith("/")) performCommand(message.substring(1));
        else host.broadcastMessage("<" + getName() + "> " + message);
    }

    @Override
    public boolean performCommand(String command) {
        ServerPlayer player = online();
        if (player == null || command == null) return false;
        String raw = command.startsWith("/") ? command.substring(1) : command;
        host.minecraft().getCommands().performPrefixedCommand(player.createCommandSourceStack(), raw);
        return true;
    }

    @Override
    public InetSocketAddress getAddress() {
        ServerPlayer player = online();
        if (player == null || player.connection == null || !(player.connection.getRemoteAddress() instanceof InetSocketAddress address)) return null;
        return address;
    }

    @Override
    public boolean isSneaking() {
        ServerPlayer player = online();
        return player != null && player.isShiftKeyDown();
    }

    @Override
    public boolean isSprinting() {
        ServerPlayer player = online();
        return player != null && player.isSprinting();
    }

    @Override
    public void setSneaking(boolean sneak) {
        ServerPlayer player = online();
        if (player != null) player.setShiftKeyDown(sneak);
    }

    @Override
    public void setSprinting(boolean sprint) {
        ServerPlayer player = online();
        if (player != null) player.setSprinting(sprint);
    }

    @Override
    public Location getLocation() {
        ServerPlayer player = online();
        if (player == null) return new Location(null, 0, 0, 0);
        return new Location(host.world(player.serverLevel()), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
    }

    @Override
    public World getWorld() {
        ServerPlayer player = online();
        return player == null ? null : host.world(player.serverLevel());
    }

    @Override
    public boolean teleport(Location location) {
        ServerPlayer player = online();
        if (player == null || location == null) return false;
        ServerLevel level = location.getWorld() instanceof NeoWorld world ? world.level() : player.serverLevel();
        if (level == null) return false;
        player.teleportTo(level, location.getX(), location.getY(), location.getZ(), Set.of(), location.getYaw(), location.getPitch());
        return true;
    }

    @Override
    public boolean isDead() {
        ServerPlayer player = online();
        return player == null || !player.isAlive();
    }

    @Override
    public boolean isValid() {
        return online() != null;
    }

    @Override
    public Spigot spigot() {
        return new Spigot() {
            @Override
            public void sendMessage(BaseComponent component) {
                NeoPlayer.this.sendMessage(component);
            }

            @Override
            public void sendMessage(BaseComponent... components) {
                NeoPlayer.this.sendMessage(components);
            }
        };
    }
}

final class NeoInventory implements PlayerInventory {
    private final NeoPlayer player;

    NeoInventory(NeoPlayer player) {
        this.player = player;
    }

    private ServerPlayer online() {
        return player.online();
    }

    @Override
    public int getSize() {
        return 41;
    }

    @Override
    public ItemStack getItem(int index) {
        ServerPlayer serverPlayer = online();
        if (serverPlayer == null || index < 0 || index >= serverPlayer.getInventory().getContainerSize()) return null;
        return McItems.fromMc(serverPlayer.getInventory().getItem(index));
    }

    @Override
    public void setItem(int index, ItemStack item) {
        ServerPlayer serverPlayer = online();
        if (serverPlayer == null || index < 0 || index >= serverPlayer.getInventory().getContainerSize()) return;
        serverPlayer.getInventory().setItem(index, McItems.toMc(item));
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        ServerPlayer serverPlayer = online();
        if (items == null) return leftover;
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType().isAir()) continue;
            if (serverPlayer == null || !serverPlayer.getInventory().add(McItems.toMc(item))) leftover.put(i, item.clone());
        }
        return leftover;
    }

    @Override
    public void clear() {
        ServerPlayer serverPlayer = online();
        if (serverPlayer != null) serverPlayer.getInventory().clearContent();
    }

    @Override
    public ItemStack getItemInMainHand() {
        ServerPlayer serverPlayer = online();
        return serverPlayer == null ? new ItemStack(Material.AIR) : McItems.fromMc(serverPlayer.getMainHandItem());
    }

    @Override
    public void setItemInMainHand(ItemStack item) {
        ServerPlayer serverPlayer = online();
        if (serverPlayer != null) serverPlayer.setItemSlot(EquipmentSlot.MAINHAND, McItems.toMc(item));
    }

    @Override
    public ItemStack getItemInOffHand() {
        ServerPlayer serverPlayer = online();
        return serverPlayer == null ? new ItemStack(Material.AIR) : McItems.fromMc(serverPlayer.getOffhandItem());
    }

    @Override
    public void setItemInOffHand(ItemStack item) {
        ServerPlayer serverPlayer = online();
        if (serverPlayer != null) serverPlayer.setItemSlot(EquipmentSlot.OFFHAND, McItems.toMc(item));
    }

    @Override
    public int getHeldItemSlot() {
        ServerPlayer serverPlayer = online();
        return serverPlayer == null ? 0 : serverPlayer.getInventory().selected;
    }

    @Override
    public void setHeldItemSlot(int slot) {
        ServerPlayer serverPlayer = online();
        if (serverPlayer != null && slot >= 0 && slot < 9) serverPlayer.getInventory().selected = slot;
    }
}

final class NeoWorld implements World {
    final NeoServer host;
    final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> key;
    private final String name;
    private final UUID uid;

    NeoWorld(NeoServer host, ServerLevel level, String name) {
        this.host = host;
        this.key = level.dimension();
        this.name = name;
        this.uid = UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    ServerLevel level() {
        return host.minecraft().getLevel(key);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUID() {
        return uid;
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return new NeoBlock(this, x, y, z);
    }

    @Override
    public Block getBlockAt(Location location) {
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public List<org.bukkit.entity.Player> getPlayers() {
        List<org.bukkit.entity.Player> players = new ArrayList<>();
        for (org.bukkit.entity.Player player : host.getOnlinePlayers()) {
            if (player.getWorld() != null && name.equals(player.getWorld().getName())) players.add(player);
        }
        return players;
    }

    @Override
    public long getTime() {
        ServerLevel level = level();
        return level == null ? 0 : level.getDayTime() % 24000L;
    }

    @Override
    public void setTime(long time) {
        ServerLevel level = level();
        if (level != null) level.setDayTime(time);
    }

    @Override
    public long getFullTime() {
        ServerLevel level = level();
        return level == null ? 0 : level.getDayTime();
    }

    @Override
    public Location getSpawnLocation() {
        ServerLevel level = level();
        if (level == null) return new Location(this, 0, 64, 0);
        BlockPos pos = level.getSharedSpawnPos();
        return new Location(this, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }
}

final class NeoBlock implements Block {
    private final NeoWorld world;
    private final int x;
    private final int y;
    private final int z;

    NeoBlock(NeoWorld world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private BlockState state() {
        ServerLevel level = world.level();
        if (level == null) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        return level.getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public Material getType() {
        return McItems.materialOf(state());
    }

    @Override
    public void setType(Material type) {
        ServerLevel level = world.level();
        if (level == null) return;
        level.setBlockAndUpdate(new BlockPos(x, y, z), McItems.stateOf(type));
    }

    @Override
    public Location getLocation() {
        return new Location(world, x, y, z);
    }

    @Override
    public org.bukkit.World getWorld() {
        return world;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public boolean isEmpty() {
        return getType().isAir();
    }

    @Override
    public Block getRelative(BlockFace face) {
        if (face == null) return this;
        return switch (face) {
            case NORTH -> getRelative(0, 0, -1);
            case SOUTH -> getRelative(0, 0, 1);
            case WEST -> getRelative(-1, 0, 0);
            case EAST -> getRelative(1, 0, 0);
            case UP -> getRelative(0, 1, 0);
            case DOWN -> getRelative(0, -1, 0);
            case NORTH_EAST -> getRelative(1, 0, -1);
            case NORTH_WEST -> getRelative(-1, 0, -1);
            case SOUTH_EAST -> getRelative(1, 0, 1);
            case SOUTH_WEST -> getRelative(-1, 0, 1);
            case SELF -> this;
        };
    }

    @Override
    public Block getRelative(int modX, int modY, int modZ) {
        return new NeoBlock(world, x + modX, y + modY, z + modZ);
    }
}
