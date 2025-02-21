package net.cloud.custom_advancements;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// The value here should match an entry in the META-INF/mods.toml file
public class CustomAdvancements {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "custom_advancements";
    // Directly reference a slf4j logger
    // Create a Deferred Register to hold Blocks which will all be registered under the "custom_advancements" namespace
    public static final Logger LOGGER = LogManager.getLogger(CustomAdvancements.class);

    public static File advFile = new File("null");
    private static final String defaultJson = """
			{
			  "minecraft:story/smelt_iron": {
			    "message": "§dAdvancement Â» ${player} achieved ${advancement}!",
			    "commands": ["give ${player} minecraft:diamond 1"]
			  }
			}""";

    public static void init() {
        LOGGER.info("Custom Advancements Init");
        try {
            if (!advFile.exists()) {
                advFile.getParentFile().mkdirs();
                advFile.createNewFile();
                FileWriter writer = new FileWriter(advFile);
                writer.write(defaultJson);
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
