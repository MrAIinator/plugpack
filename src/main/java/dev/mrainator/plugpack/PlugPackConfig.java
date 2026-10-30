package dev.mrainator.plugpack;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PlugPackConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.ConfigValue<String> FOLDER;
    public static final ModConfigSpec.BooleanValue OP_ALL;
    public static final ModConfigSpec.BooleanValue MOVE_EVENTS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("PlugPack server plugin loader").push("general");
        ENABLED = builder.comment("Load jars from the plugins folder when the dedicated server starts.").define("enabled", true);
        FOLDER = builder.comment("Plugin folder, relative to the server directory.").define("pluginsFolder", "plugins");
        OP_ALL = builder.comment("Server operators pass every permission check.").define("opHasAllPermissions", true);
        MOVE_EVENTS = builder.comment("Fire PlayerMoveEvent. Off by default because it runs often.").define("fireMoveEvent", false);
        builder.pop();
        SPEC = builder.build();
    }

    private PlugPackConfig() {
    }
}
