package net.cloud.custom_advancements.forge;

import dev.architectury.platform.forge.EventBuses;
import net.cloud.custom_advancements.CustomAdvancements;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod(CustomAdvancements.MOD_ID)
public class CustomAdvancementsForge {
    public CustomAdvancementsForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(CustomAdvancements.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        CustomAdvancements.advFile = new File((FMLPaths.GAMEDIR.get().toString() + "/config/"), File.separator + "custom_advancements.json");
        CustomAdvancements.init();
    }
}
