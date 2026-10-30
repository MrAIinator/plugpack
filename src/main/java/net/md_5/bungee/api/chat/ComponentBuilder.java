package net.md_5.bungee.api.chat;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;

public class ComponentBuilder {
    private final List<TextComponent> parts = new ArrayList<>();

    public ComponentBuilder(String text) {
        parts.add(new TextComponent(text));
    }

    public ComponentBuilder append(String text) {
        parts.add(new TextComponent(text));
        return this;
    }

    public ComponentBuilder color(ChatColor color) {
        current().setColor(color);
        return this;
    }

    public ComponentBuilder bold(boolean bold) {
        current().setBold(bold);
        return this;
    }

    public ComponentBuilder event(ClickEvent event) {
        current().setClickEvent(event);
        return this;
    }

    public BaseComponent[] create() {
        return parts.toArray(BaseComponent[]::new);
    }

    private TextComponent current() {
        return parts.get(parts.size() - 1);
    }
}
