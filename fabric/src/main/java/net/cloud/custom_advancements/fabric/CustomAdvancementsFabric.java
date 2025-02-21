package net.cloud.custom_advancements.fabric;

import net.cloud.custom_advancements.CustomAdvancements;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.File;

public class CustomAdvancementsFabric implements ModInitializer {



    @Override
    public void onInitialize() {
        CustomAdvancements.advFile = new File((FabricLoader.getInstance().getGameDir() + "/config/"), File.separator + "custom_advancements.json");
        CustomAdvancements.init();

    }
}
