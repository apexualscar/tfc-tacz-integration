package com.tacz.tfcintegration.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tacz.tfcintegration.config.ModConfig;

public class RecipeTransformer {

    public static JsonObject transform(JsonObject recipe, String tier) {
        String metal = ModConfig.getMetalForTier(tier);
        String metalName = metal.contains("/") ? metal.substring(metal.lastIndexOf('/') + 1) : metal;

        if (recipe.has("materials")) {
            transformMaterials(recipe.getAsJsonArray("materials"), metal, metalName);
        }

        if (recipe.has("key")) {
            transformCraftingKey(recipe.getAsJsonObject("key"), metal, metalName);
        }

        return recipe;
    }

    private static void transformMaterials(JsonArray materials, String metal, String metalName) {
        for (JsonElement element : materials) {
            if (!element.isJsonObject()) continue;
            JsonObject mat = element.getAsJsonObject();

            if (mat.has("item")) {
                JsonObject itemObj = mat.getAsJsonObject("item");
                if (itemObj.has("tag")) {
                    String tag = itemObj.get("tag").getAsString();
                    if ("forge:ingots/iron".equals(tag)) {
                        itemObj.remove("tag");
                        itemObj.addProperty("item", metal);
                    } else if ("forge:nuggets/iron".equals(tag)) {
                        itemObj.remove("tag");
                        replaceNuggets(itemObj, mat, metal, metalName);
                    }
                }
            }
        }
    }

    private static void replaceNuggets(JsonObject itemObj, JsonObject mat, String metal, String metalName) {
        if ("minecraft:netherite_ingot".equals(metal)) {
            itemObj.addProperty("item", "minecraft:netherite_scrap");
            return;
        }
        itemObj.addProperty("item", "tfc:metal/ingot/" + metalName);
        if (mat.has("count")) {
            int count = mat.get("count").getAsInt();
            mat.addProperty("count", Math.max(1, Math.round(count / 9.0f)));
        }
    }

    private static void transformCraftingKey(JsonObject key, String metal, String metalName) {
        for (String k : key.keySet()) {
            JsonElement elem = key.get(k);
            if (!elem.isJsonObject()) continue;

            JsonObject slot = elem.getAsJsonObject();
            if (slot.has("tag")) {
                String tag = slot.get("tag").getAsString();
                if ("forge:ingots/iron".equals(tag)) {
                    slot.remove("tag");
                    slot.addProperty("item", metal);
                } else if ("forge:nuggets/iron".equals(tag)) {
                    slot.remove("tag");
                    if ("minecraft:netherite_ingot".equals(metal)) {
                        slot.addProperty("item", "minecraft:netherite_scrap");
                    } else {
                        slot.addProperty("item", "tfc:metal/ingot/" + metalName);
                    }
                }
            } else if (slot.has("item")) {
                String item = slot.get("item").getAsString();
                if ("minecraft:iron_block".equals(item)) {
                    if ("minecraft:netherite_ingot".equals(metal)) {
                        slot.addProperty("item", "minecraft:netherite_block");
                    } else {
                        slot.addProperty("item", "tfc:metal/block/" + metalName);
                    }
                }
            }
        }
    }
}
