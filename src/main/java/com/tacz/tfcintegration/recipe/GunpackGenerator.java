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

    private static final String[] CATEGORIES = {"guns", "ammo", "attachments"};

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

            count += GunDataGenerator.generate();

            if (count > 0) {
                Files.writeString(sentinel, String.valueOf(System.currentTimeMillis()));
            }
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
        String dirName = "guns".equals(category) ? "gun" : category;
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
                original = readBundledRecipe(dirName, recipeId);
            }
            if (original == null) {
                LOGGER.warn("Default recipe not found: {}/{}", category, recipeId);
                continue;
            }

            RecipeTransformer.transform(original, tier, category);
            Path out = outDir.resolve(recipeId + ".json");
            Files.writeString(out, GSON.toJson(original));
            count++;
        }

        return count;
    }

    private static JsonObject readDefaultRecipe(String category, String recipeId) {
        String dirName = "guns".equals(category) ? "gun" : category;
        String fileName = recipeId + ".json";

        Path unpacked = FMLPaths.GAMEDIR.get().resolve("tacz").resolve("tacz_default_gun")
            .resolve("data").resolve("tacz").resolve("recipes").resolve(dirName).resolve(fileName);
        JsonObject recipe = readJson(unpacked);
        if (recipe != null) return recipe;

        var modFile = ModList.get().getModFileById("tacz");
        if (modFile != null) {
            for (String prefix : new String[]{
                "assets/tacz/custom/tacz_default_gun/data/tacz/recipes/",
                "data/tacz/recipes/"
            }) {
                var resource = modFile.getFile().findResource(prefix + dirName + "/" + fileName);
                if (resource != null) {
                    try (InputStream in = Files.newInputStream(resource)) {
                        return JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
                    } catch (Exception e) {
                        LOGGER.debug("Failed to read default recipe {}/{}: {}", category, recipeId, e.getMessage());
                        return null;
                    }
                }
            }
        }

        return null;
    }

    // Recipes with no default in the TACZ pack (e.g. loot-only guns) are bundled
    // as resources and carried through verbatim instead of being skipped.
    private static JsonObject readBundledRecipe(String dirName, String recipeId) {
        try (InputStream in = GunpackGenerator.class.getResourceAsStream(
                "/data/tacz_tfc_integration/static_recipes/" + dirName + "/" + recipeId + ".json")) {
            if (in == null) return null;
            return JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static JsonObject readJson(Path path) {
        if (!Files.exists(path)) return null;
        try (InputStream in = Files.newInputStream(path)) {
            return JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.debug("Failed to read default recipe {}: {}", path, e.getMessage());
            return null;
        }
    }
}
