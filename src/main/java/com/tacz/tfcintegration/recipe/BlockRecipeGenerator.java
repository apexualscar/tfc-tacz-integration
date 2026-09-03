package com.tacz.tfcintegration.recipe;

import com.google.gson.*;
import com.tacz.tfcintegration.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.nio.file.*;

public class BlockRecipeGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String[][] BLOCK_RECIPES = {
        {"gun_smith_table", "crafting_shaped"},
        {"iron_ammo_box", "crafting_shaped"},
        {"attachment_workbench", "crafting_shaped"},
        {"ammo_workbench", "crafting_shaped"},
        {"target", "crafting_shaped"}
    };

    public static void generate() {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path recipesDir = gameDir.resolve("data").resolve("tacz").resolve("recipes");

        try {
            Files.createDirectories(recipesDir);

            for (String[] recipe : BLOCK_RECIPES) {
                String recipeId = recipe[0];
                String tier = ModConfig.getTierForBlockRecipe(recipeId);
                JsonObject generated = generateBlockRecipe(recipeId, tier);
                if (generated != null) {
                    Files.writeString(recipesDir.resolve(recipeId + ".json"), GSON.toJson(generated));
                }
            }

            LOGGER.info("Generated block recipes to {}", recipesDir);
        } catch (IOException e) {
            LOGGER.error("Failed to generate block recipes", e);
        }
    }

    private static JsonObject generateBlockRecipe(String recipeId, String tier) {
        String metal = ModConfig.getMetalForTier(tier);
        String metalName = metal.contains("/") ? metal.substring(metal.lastIndexOf('/') + 1) : metal;
        String blockMetal = "minecraft:netherite_ingot".equals(metal) ? "minecraft:netherite_block" : "tfc:metal/block/" + metalName;

        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");

        JsonObject result = new JsonObject();
        result.addProperty("item", "tacz:" + recipeId);
        recipe.add("result", result);

        switch (recipeId) {
            case "gun_smith_table" -> {
                recipe.add("pattern", createArray("LLL", "IBI", "I I"));
                JsonObject key = new JsonObject();
                key.add("L", tagSlot("minecraft:logs"));
                key.add("I", itemSlot(metal));
                key.add("B", itemSlot(blockMetal));
                recipe.add("key", key);
            }
            case "iron_ammo_box" -> {
                recipe.add("pattern", createArray("III", "ILI", "III"));
                JsonObject key = new JsonObject();
                key.add("I", itemSlot(metal));
                key.add("L", tagSlot("minecraft:logs"));
                recipe.add("key", key);
            }
            case "attachment_workbench" -> {
                recipe.add("pattern", createArray("WIW", "ILI", "WBW"));
                JsonObject key = new JsonObject();
                key.add("W", tagSlot("minecraft:planks"));
                key.add("I", itemSlot(metal));
                key.add("L", tagSlot("minecraft:logs"));
                key.add("B", itemSlot(blockMetal));
                recipe.add("key", key);
            }
            case "ammo_workbench" -> {
                recipe.add("pattern", createArray("WIW", "ILI", "WIW"));
                JsonObject key = new JsonObject();
                key.add("W", tagSlot("minecraft:planks"));
                key.add("I", itemSlot(metal));
                key.add("L", tagSlot("minecraft:logs"));
                recipe.add("key", key);
            }
            case "target" -> {
                recipe.add("pattern", createArray("WIW", "I I", "WIW"));
                JsonObject key = new JsonObject();
                key.add("W", tagSlot("minecraft:planks"));
                key.add("I", itemSlot(metal));
                recipe.add("key", key);
            }
            default -> {
                LOGGER.warn("Unknown block recipe: {}", recipeId);
                return null;
            }
        }

        return recipe;
    }

    private static JsonArray createArray(String... lines) {
        JsonArray arr = new JsonArray();
        for (String line : lines) arr.add(line);
        return arr;
    }

    private static JsonObject tagSlot(String tag) {
        JsonObject obj = new JsonObject();
        obj.addProperty("tag", tag);
        return obj;
    }

    private static JsonObject itemSlot(String item) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", item);
        return obj;
    }
}
