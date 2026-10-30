package org.bukkit;

public enum GameMode {
    CREATIVE,
    SURVIVAL,
    ADVENTURE,
    SPECTATOR;

    public static GameMode getByValue(int value) {
        return switch (value) {
            case 1 -> CREATIVE;
            case 2 -> ADVENTURE;
            case 3 -> SPECTATOR;
            default -> SURVIVAL;
        };
    }

    public int getValue() {
        return switch (this) {
            case CREATIVE -> 1;
            case ADVENTURE -> 2;
            case SPECTATOR -> 3;
            case SURVIVAL -> 0;
        };
    }
}
