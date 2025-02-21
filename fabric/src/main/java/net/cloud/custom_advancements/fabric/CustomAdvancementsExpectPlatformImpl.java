package net.cloud.custom_advancements.fabric;

import net.cloud.custom_advancements.CustomAdvancementsExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class CustomAdvancementsExpectPlatformImpl {
    /**
     * This is our actual method to {@link CustomAdvancementsExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
