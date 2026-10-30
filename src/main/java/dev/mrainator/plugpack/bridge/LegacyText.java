package dev.mrainator.plugpack.bridge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class LegacyText {
    private LegacyText() {
    }

    public static Component toComponent(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        MutableComponent root = Component.literal("");
        Style style = Style.EMPTY;
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char character = raw.charAt(i);
            if (character == '\u00A7' && i + 1 < raw.length()) {
                flush(root, buffer, style);
                style = apply(style, raw.charAt(++i));
            } else {
                buffer.append(character);
            }
        }
        flush(root, buffer, style);
        return root;
    }

    public static String plain(String raw) {
        return org.bukkit.ChatColor.stripColor(raw == null ? "" : raw);
    }

    private static void flush(MutableComponent root, StringBuilder buffer, Style style) {
        if (buffer.isEmpty()) return;
        root.append(Component.literal(buffer.toString()).setStyle(style));
        buffer.setLength(0);
    }

    private static Style apply(Style style, char code) {
        char lower = Character.toLowerCase(code);
        if (lower == 'r') return Style.EMPTY;
        ChatFormatting formatting = switch (lower) {
            case '0' -> ChatFormatting.BLACK;
            case '1' -> ChatFormatting.DARK_BLUE;
            case '2' -> ChatFormatting.DARK_GREEN;
            case '3' -> ChatFormatting.DARK_AQUA;
            case '4' -> ChatFormatting.DARK_RED;
            case '5' -> ChatFormatting.DARK_PURPLE;
            case '6' -> ChatFormatting.GOLD;
            case '7' -> ChatFormatting.GRAY;
            case '8' -> ChatFormatting.DARK_GRAY;
            case '9' -> ChatFormatting.BLUE;
            case 'a' -> ChatFormatting.GREEN;
            case 'b' -> ChatFormatting.AQUA;
            case 'c' -> ChatFormatting.RED;
            case 'd' -> ChatFormatting.LIGHT_PURPLE;
            case 'e' -> ChatFormatting.YELLOW;
            case 'f' -> ChatFormatting.WHITE;
            case 'k' -> ChatFormatting.OBFUSCATED;
            case 'l' -> ChatFormatting.BOLD;
            case 'm' -> ChatFormatting.STRIKETHROUGH;
            case 'n' -> ChatFormatting.UNDERLINE;
            case 'o' -> ChatFormatting.ITALIC;
            default -> null;
        };
        if (formatting == null) return style;
        if (formatting.isColor()) return style.withColor(formatting);
        return switch (formatting) {
            case BOLD -> style.withBold(true);
            case ITALIC -> style.withItalic(true);
            case UNDERLINE -> style.withUnderlined(true);
            case STRIKETHROUGH -> style.withStrikethrough(true);
            case OBFUSCATED -> style.withObfuscated(true);
            default -> style;
        };
    }
}
