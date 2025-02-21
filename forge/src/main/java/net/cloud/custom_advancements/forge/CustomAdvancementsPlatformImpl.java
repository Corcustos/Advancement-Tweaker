package net.cloud.custom_advancements.forge;

import net.cloud.custom_advancements.CustomAdvancementsExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class CustomAdvancementsPlatformImpl {
    /**
     * This is our actual method to {@link CustomAdvancementsExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
