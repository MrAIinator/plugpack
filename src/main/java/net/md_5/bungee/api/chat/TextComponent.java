package net.md_5.bungee.api.chat;

public class TextComponent extends BaseComponent {
    public TextComponent() {
    }

    public TextComponent(String text) {
        this.text = text == null ? "" : text;
    }

    public TextComponent(BaseComponent component) {
        this.text = component == null ? "" : component.toLegacyText();
    }

    public static String toLegacyText(BaseComponent... components) {
        return BaseComponent.toLegacyText(components);
    }

    public static String toPlainText(BaseComponent... components) {
        return org.bukkit.ChatColor.stripColor(toLegacyText(components));
    }

    @Override
    public TextComponent duplicate() {
        return new TextComponent(toLegacyText());
    }
}
