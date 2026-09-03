package com.tacz.tfcintegration.recipe;

import com.google.gson.*;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

public class GunpackGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String GUNPACK_NAME = "zz_tacz_tfc_integration";

    private static final String[] CATEGORIES = {"gun", "ammo", "attachments"};

    public static void generate() {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path gunpackDir = gameDir.resolve("tacz").resolve(GUNPACK_NAME);

        Path sentinel = com.tacz.tfcintegration.config.ModConfig.getConfigDir().resolve(".last_generated");
        if (Files.exists(sentinel)) {
            try {
                long configTime = Files.getLastModifiedTime(
                    com.tacz.tfcintegration.config.ModConfig.getConfigDir().resolve("recipe-tiers.toml")
                ).toMillis();
                long sentinelTime = Files.getLastModifiedTime(sentinel).toMillis();
                if (sentinelTime >= configTime && Files.exists(gunpackDir.resolve("gunpack.meta.json"))) {
                    LOGGER.info("Gunpack up to date, skipping generation");
                    return;
                }
            } catch (IOException e) {
                // regenerate
            }
        }

        LOGGER.info("Generating gunpack to {}", gunpackDir);

        try {
            Files.createDirectories(gunpackDir);
            writeMeta(gunpackDir);
            int count = 0;

            for (String category : CATEGORIES) {
                count += generateCategory(category, gunpackDir);
            }

            Files.writeString(sentinel, String.valueOf(System.currentTimeMillis()));
            LOGGER.info("Generated {} override recipes", count);
        } catch (IOException e) {
            LOGGER.error("Failed to generate gunpack", e);
        }
    }

    private static void writeMeta(Path gunpackDir) throws IOException {
        JsonObject meta = new JsonObject();
        meta.addProperty("namespace", "tacz");
        Files.writeString(gunpackDir.resolve("gunpack.meta.json"), GSON.toJson(meta));

        JsonObject pack = new JsonObject();
        JsonObject packObj = new JsonObject();
        packObj.addProperty("description", "TACZ TFC Integration");
        packObj.addProperty("pack_format", 15);
        pack.add("pack", packObj);
        Files.writeString(gunpackDir.resolve("pack.mcmeta"), GSON.toJson(pack));
    }

    private static int generateCategory(String category, Path gunpackDir) throws IOException {
        String dirName = "gun".equals(category) ? "gun" : category;
        Path outDir = gunpackDir.resolve("data").resolve("tacz").resolve("recipes").resolve(dirName);
        Files.createDirectories(outDir);

        int count = 0;
        Map<String, String> recipeTiers = com.tacz.tfcintegration.config.ModConfig.getRecipeTiers();
        String prefix = category + "/";

        for (Map.Entry<String, String> entry : recipeTiers.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(prefix)) continue;

            String recipeId = key.substring(prefix.length());
            String tier = entry.getValue();

            JsonObject original = readDefaultRecipe(category, recipeId);
            if (original == null) {
                LOGGER.warn("Default recipe not found: {}/{}", category, recipeId);
                continue;
            }

            RecipeTransformer.transform(original, tier);
            Path out = outDir.resolve(recipeId + ".json");
            Files.writeString(out, GSON.toJson(original));
            count++;
        }

        return count;
    }

    private static JsonObject readDefaultRecipe(String category, String recipeId) {
        try {
            var modFile = ModList.get().getModFileById("tacz");
            if (modFile == null) {
                LOGGER.warn("TACZ mod not found");
                return null;
            }

            String dirName = "gun".equals(category) ? "gun" : category;
            String path = "data/tacz/recipes/" + dirName + "/" + recipeId + ".json";
            var resource = modFile.getFile().findResource(path);
            if (resource == null) return null;

            try (InputStream in = Files.newInputStream(resource)) {
                return JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to read default recipe {}/{}: {}", category, recipeId, e.getMessage());
            return null;
        }
    }
}
