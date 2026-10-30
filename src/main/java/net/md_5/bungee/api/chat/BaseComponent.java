package net.md_5.bungee.api.chat;

public class BaseComponent {
    protected String text = "";
    protected String color;
    protected Boolean bold;
    protected Boolean italic;
    protected Boolean underlined;
    protected ClickEvent clickEvent;
    protected HoverEvent hoverEvent;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public void setColor(net.md_5.bungee.api.ChatColor color) {
        this.color = color == null ? null : color.toString();
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    public void setUnderlined(Boolean underlined) {
        this.underlined = underlined;
    }

    public void setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }

    public void setHoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
    }

    public ClickEvent getClickEvent() {
        return clickEvent;
    }

    public HoverEvent getHoverEvent() {
        return hoverEvent;
    }

    public String toLegacyText() {
        StringBuilder out = new StringBuilder();
        if (color != null) out.append(color);
        if (Boolean.TRUE.equals(bold)) out.append(net.md_5.bungee.api.ChatColor.BOLD);
        if (Boolean.TRUE.equals(italic)) out.append(net.md_5.bungee.api.ChatColor.ITALIC);
        if (Boolean.TRUE.equals(underlined)) out.append(net.md_5.bungee.api.ChatColor.UNDERLINE);
        out.append(text == null ? "" : text);
        return out.toString();
    }

    public static String toLegacyText(BaseComponent[] components) {
        if (components == null) return "";
        StringBuilder out = new StringBuilder();
        for (BaseComponent component : components) if (component != null) out.append(component.toLegacyText());
        return out.toString();
    }

    public BaseComponent duplicate() {
        BaseComponent copy = new BaseComponent();
        copy.text = text;
        copy.color = color;
        copy.bold = bold;
        copy.italic = italic;
        copy.underlined = underlined;
        copy.clickEvent = clickEvent;
        copy.hoverEvent = hoverEvent;
        return copy;
    }
}
