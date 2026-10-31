package dev.mrainator.plugpack.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Small YAML subset reader/writer: maps, lists, scalars. Enough for plugin.yml and config.yml. */
public final class YamlParser {
    private YamlParser() {
    }

    public static Map<String, Object> parse(String text) {
        List<Line> lines = new ArrayList<>();
        if (text != null) {
            String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
            for (String raw : normalized.split("\n", -1)) {
                raw = raw.replace("\t", "  ");
                String stripped = stripComment(raw);
                if (stripped.isBlank()) continue;
                int indent = 0;
                while (indent < stripped.length() && stripped.charAt(indent) == ' ') indent++;
                lines.add(new Line(indent, stripped.substring(indent).trim()));
            }
        }
        if (lines.isEmpty()) return new LinkedHashMap<>();
        int[] index = {0};
        if (lines.get(0).body.startsWith("-")) {
            throw new IllegalArgumentException("YAML root must be a map");
        }
        Object value = parseMap(lines, index, lines.get(0).indent);
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) out.put(String.valueOf(entry.getKey()), entry.getValue());
            return out;
        }
        return new LinkedHashMap<>();
    }

    public static String emit(Map<String, Object> map) {
        StringBuilder out = new StringBuilder();
        emitMap(out, map, 0);
        return out.toString();
    }

    private static Map<String, Object> parseMap(List<Line> lines, int[] index, int indent) {
        Map<String, Object> map = new LinkedHashMap<>();
        while (index[0] < lines.size()) {
            Line line = lines.get(index[0]);
            if (line.indent < indent) break;
            if (line.body.startsWith("- ")) break;
            if (line.indent > indent) {
                throw new IllegalArgumentException("Bad indent near: " + line.body);
            }
            int colon = splitKey(line.body);
            if (colon < 0) throw new IllegalArgumentException("Expected key near: " + line.body);
            String key = unquote(line.body.substring(0, colon).trim());
            String rest = line.body.substring(colon + 1).trim();
            index[0]++;
            map.put(key, nested(lines, index, indent, rest));
        }
        return map;
    }

    private static List<Object> parseList(List<Line> lines, int[] index, int indent) {
        List<Object> list = new ArrayList<>();
        while (index[0] < lines.size()) {
            Line line = lines.get(index[0]);
            if (line.indent < indent) break;
            if (line.indent > indent) throw new IllegalArgumentException("Bad list indent near: " + line.body);
            if (!line.body.equals("-") && !line.body.startsWith("- ")) break;
            String rest = line.body.equals("-") ? "" : line.body.substring(2).trim();
            index[0]++;
            if (!rest.isEmpty() && splitKey(rest) >= 0 && !isQuoted(rest)) {
                Map<String, Object> map = new LinkedHashMap<>();
                putInline(map, rest);
                if (index[0] < lines.size() && lines.get(index[0]).indent > indent && !lines.get(index[0]).body.startsWith("- ")) {
                    int child = lines.get(index[0]).indent;
                    map.putAll(parseMap(lines, index, child));
                }
                list.add(map);
            } else {
                list.add(nested(lines, index, indent, rest));
            }
        }
        return list;
    }

    private static Object nested(List<Line> lines, int[] index, int indent, String rest) {
        if (rest.isEmpty() || rest.equals("|") || rest.equals(">")) {
            if (index[0] >= lines.size()) return "";
            Line next = lines.get(index[0]);
            if (next.body.startsWith("-") && next.indent >= indent) {
                return parseList(lines, index, next.indent);
            }
            if (next.indent <= indent) return "";
            int child = next.indent;
            if (rest.equals("|") || rest.equals(">")) {
                StringBuilder block = new StringBuilder();
                while (index[0] < lines.size() && lines.get(index[0]).indent >= child) {
                    if (!block.isEmpty()) block.append('\n');
                    block.append(lines.get(index[0]).body);
                    index[0]++;
                }
                return block.toString();
            }
            if (lines.get(index[0]).body.startsWith("-")) return parseList(lines, index, child);
            return parseMap(lines, index, child);
        }
        return scalar(rest);
    }

    private static void putInline(Map<String, Object> map, String rest) {
        int colon = splitKey(rest);
        String key = unquote(rest.substring(0, colon).trim());
        String value = rest.substring(colon + 1).trim();
        map.put(key, value.isEmpty() ? "" : scalar(value));
    }

    private static Object scalar(String raw) {
        String text = raw.trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            String inner = text.substring(1, text.length() - 1).trim();
            if (inner.isEmpty()) return List.of();
            List<Object> list = new ArrayList<>();
            for (String part : inner.split(",")) {
                if (!part.isBlank()) list.add(scalar(part.trim()));
            }
            return list;
        }
        if ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("'") && text.endsWith("'"))) {
            return unquote(text);
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.equals("true") || lower.equals("yes") || lower.equals("on")) return Boolean.TRUE;
        if (lower.equals("false") || lower.equals("no") || lower.equals("off")) return Boolean.FALSE;
        if (lower.equals("null") || lower.equals("~") || lower.equals("none")) return null;
        if (text.matches("-?\\d+")) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                try {
                    return Long.parseLong(text);
                } catch (NumberFormatException ignoredLong) {
                    return text;
                }
            }
        }
        if (text.matches("-?\\d+\\.\\d+")) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return text;
            }
        }
        return text;
    }

    private static int splitKey(String body) {
        boolean quote = false;
        char q = 0;
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if ((c == '"' || c == '\'') && (i == 0 || body.charAt(i - 1) != '\\')) {
                if (!quote) {
                    quote = true;
                    q = c;
                } else if (c == q) quote = false;
            } else if (c == ':' && !quote) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isQuoted(String text) {
        return (text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("'") && text.endsWith("'"));
    }

    private static String unquote(String text) {
        if (text.length() >= 2 && ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("'") && text.endsWith("'")))) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private static String stripComment(String raw) {
        boolean quote = false;
        char q = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c == '"' || c == '\'') && (i == 0 || raw.charAt(i - 1) != '\\')) {
                if (!quote) {
                    quote = true;
                    q = c;
                } else if (c == q) quote = false;
            } else if (c == '#' && !quote) {
                return raw.substring(0, i);
            }
        }
        return raw;
    }

    @SuppressWarnings("unchecked")
    private static void emitMap(StringBuilder out, Map<String, Object> map, int indent) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            indent(out, indent).append(entry.getKey()).append(':');
            emitValue(out, entry.getValue(), indent);
        }
    }

    @SuppressWarnings("unchecked")
    private static void emitValue(StringBuilder out, Object value, int indent) {
        if (value instanceof Map<?, ?> map) {
            out.append('\n');
            Map<String, Object> typed = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) typed.put(String.valueOf(entry.getKey()), entry.getValue());
            emitMap(out, typed, indent + 2);
            return;
        }
        if (value instanceof List<?> list) {
            if (list.isEmpty()) {
                out.append(" []\n");
                return;
            }
            out.append('\n');
            for (Object item : list) {
                indent(out, indent + 2).append("-");
                if (item instanceof Map<?, ?>) {
                    out.append('\n');
                    Map<String, Object> typed = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                        typed.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    emitMap(out, typed, indent + 4);
                } else {
                    out.append(' ');
                    emitScalar(out, item);
                    out.append('\n');
                }
            }
            return;
        }
        out.append(' ');
        emitScalar(out, value);
        out.append('\n');
    }

    private static void emitScalar(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
            return;
        }
        if (value instanceof Boolean || value instanceof Number) {
            out.append(value);
            return;
        }
        String text = String.valueOf(value);
        if (text.isEmpty() || text.contains(":") || text.contains("#") || text.contains("\n") || text.startsWith(" ")) {
            out.append('"').append(text.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        } else {
            out.append(text);
        }
    }

    private static StringBuilder indent(StringBuilder out, int indent) {
        out.append(" ".repeat(Math.max(0, indent)));
        return out;
    }

    private record Line(int indent, String body) {
    }
}
