package com.tacz.tfcintegration.recipe;

import com.google.gson.*;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

public class BlockRecipeGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] BLOCK_IDS = {
        "gun_smith_table",
        "iron_ammo_box",
        "attachment_workbench",
        "ammo_workbench",
        "target"
    };

    public static void generate() {
        Map<String, String> tiers = com.tacz.tfcintegration.config.ModConfig.getBlockRecipeTiers();
        Path packOut = FMLPaths.GAMEDIR.get().resolve("tacz").resolve("zz_tacz_tfc_integration")
            .resolve("data").resolve("tacz").resolve("recipes");

        try {
            Files.createDirectories(packOut);
        } catch (IOException e) {
            LOGGER.error("Failed to create block recipe output dir", e);
            return;
        }

        var modFile = ModList.get().getModFileById("tacz");
        if (modFile == null) {
            LOGGER.warn("TACZ mod not found, skipping block recipes");
            return;
        }

        for (String id : BLOCK_IDS) {
            String tier = tiers.getOrDefault(id, "t1");
            JsonObject recipe = readTaczBlockRecipe(modFile, id);
            if (recipe == null) {
                LOGGER.debug("Default block recipe not found in TACZ jar: {}", id);
                continue;
            }
            RecipeTransformer.transform(recipe, tier);
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Files.writeString(packOut.resolve(id + ".json"), gson.toJson(recipe));
            } catch (IOException e) {
                LOGGER.debug("Failed to write block recipe {}: {}", id, e.getMessage());
            }
        }
        LOGGER.info("Block recipes written to {}", packOut);
    }

    private static JsonObject readTaczBlockRecipe(net.minecraftforge.forgespi.language.IModFileInfo modFile, String id) {
        String path = "data/tacz/recipes/" + id + ".json";
        var resource = modFile.getFile().findResource(path);
        if (resource == null) return null;
        try (InputStream in = Files.newInputStream(resource)) {
            return JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.debug("Failed to read TACZ block recipe {}: {}", id, e.getMessage());
            return null;
        }
    }
}
