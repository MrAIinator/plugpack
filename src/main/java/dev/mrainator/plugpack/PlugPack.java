package dev.mrainator.plugpack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PlugPack.MOD_ID)
public class PlugPack {
    public static final String MOD_ID = "plugpack";
    public static final Logger LOGGER = LoggerFactory.getLogger("PlugPack");

    public PlugPack(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, PlugPackConfig.SPEC);
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            LOGGER.info("PlugPack is dedicated-server only and will not load plugins here.");
        }
    }
}
