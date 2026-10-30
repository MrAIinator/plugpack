package org.bukkit;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum ChatColor {
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    MAGIC('k'),
    BOLD('l'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r');

    public static final char COLOR_CHAR = '\u00A7';
    private static final Map<Character, ChatColor> BY_CHAR = new HashMap<>();

    static {
        for (ChatColor color : values()) {
            BY_CHAR.put(color.code, color);
        }
    }

    private final char code;

    ChatColor(char code) {
        this.code = code;
    }

    public char getChar() {
        return code;
    }

    public boolean isFormat() {
        return code >= 'k' && code <= 'o';
    }

    public boolean isColor() {
        return !isFormat() && this != RESET;
    }

    @Override
    public String toString() {
        return new String(new char[]{COLOR_CHAR, code});
    }

    public static ChatColor getByChar(char code) {
        return BY_CHAR.get(Character.toLowerCase(code));
    }

    public static String translateAlternateColorCodes(char alt, String text) {
        if (text == null) return null;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == alt && BY_CHAR.containsKey(Character.toLowerCase(chars[i + 1]))) {
                chars[i] = COLOR_CHAR;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    public static String stripColor(String input) {
        if (input == null) return null;
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == COLOR_CHAR && i + 1 < input.length()) {
                i++;
                continue;
            }
            out.append(input.charAt(i));
        }
        return out.toString();
    }

    public static String valueOfName(String name) {
        if (name == null || name.isEmpty()) return null;
        if (name.length() == 1) {
            ChatColor color = BY_CHAR.get(Character.toLowerCase(name.charAt(0)));
            if (color != null) return color.toString();
        }
        try {
            return valueOf(name.toUpperCase(Locale.ROOT)).toString();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
