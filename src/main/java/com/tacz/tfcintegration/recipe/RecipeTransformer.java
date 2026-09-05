package com.tacz.tfcintegration.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tacz.tfcintegration.config.ModConfig;

public class RecipeTransformer {

    // Ammo material counts scale with the ammo tier: cheapest at t1 (50%),
    // full price at the top ammo tier (t4).
    private static final double[] AMMO_COST = {0.50, 0.67, 0.83, 1.00};
    private static final double END_CRYSTAL_COUNT = 128;

    // Single substitution applied to every category.
    private static String swapAnywhere(String tag) {
        switch (tag) {
            case "forge:gems/amethyst":      return "item:tfc:gem/amethyst";
            case "forge:gems/quartz":        return "item:tfc:metal/ingot/weak_steel";
            case "forge:rods/blaze":         return "item:tfc:metal/rod/red_steel";
            case "forge:ingots/netherite":   return "item:tfc:metal/ingot/blue_steel";
            case "forge:ores/netherite_scrap": return "item:tfc:metal/ingot/unknown";
            default:                          return null;
        }
    }

    // Category-scoped substitutions (guns/ammo/attachments).
    private static String swapForCategory(String category, String id) {
        if ("ammo".equals(category)) {
            switch (id) {
                case "forge:ingots/copper": return "item:tfc:metal/ingot/brass";
                case "forge:gems/lapis":    return "item:tfc:metal/ingot/bismuth";
                default:                    return null;
            }
        }
        if ("guns".equals(category) && "forge:gems/lapis".equals(id)) {
            return "item:tfc:metal/ingot/nickel";
        }
        if ("attachments".equals(category)) {
            switch (id) {
                case "forge:gems/lapis":         return "item:tfc:metal/ingot/sterling_silver";
                case "minecraft:crying_obsidian": return "item:tfc:metal/ingot/black_bronze";
                case "minecraft:ancient_debris":  return "item:tfc:metal/ingot/unknown";
                case "minecraft:end_crystal":     return "item:minecraft:gunpowder";
                default:                          return null;
            }
        }
        return null;
    }

    private static int ammoTierIndex(String tier) {
        if (tier.startsWith("t") && tier.length() == 2) {
            int n = tier.charAt(1) - '0';
            if (n >= 1 && n <= 4) return n - 1;
        }
        return -1;
    }

    private static void scaleAmmoCosts(JsonArray materials, String tier) {
        int idx = ammoTierIndex(tier);
        if (idx < 0) return;
        double factor = AMMO_COST[idx];
        if (factor >= 1.0) return;
        for (JsonElement element : materials) {
            if (!element.isJsonObject()) continue;
            JsonObject mat = element.getAsJsonObject();
            if (!mat.has("count")) continue;
            int old = mat.get("count").getAsInt();
            int next = Math.max(1, (int) Math.round(old * factor));
            if (next != old) mat.addProperty("count", next);
        }
    }

    private static String resolveSub(String category, String id) {
        String any = swapAnywhere(id);
        if (any != null) return any;
        if (category != null) return swapForCategory(category, id);
        return null;
    }

    public static JsonObject transform(JsonObject recipe, String tier) {
        return transform(recipe, tier, null);
    }

    public static JsonObject transform(JsonObject recipe, String tier, String category) {
        String metal = ModConfig.getMetalForTier(tier);
        String metalName = metal.contains("/") ? metal.substring(metal.lastIndexOf('/') + 1) : metal;

        if (recipe.has("materials")) {
            transformMaterials(recipe.getAsJsonArray("materials"), metal, metalName, category, tier);
        }

        if (recipe.has("key")) {
            transformCraftingKey(recipe.getAsJsonObject("key"), metal, metalName);
        }

        return recipe;
    }

    private static void transformMaterials(JsonArray materials, String metal, String metalName, String category, String tier) {
        if ("ammo".equals(category)) {
            scaleAmmoCosts(materials, tier);
        }
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
                    } else {
                        String sub = resolveSub(category, tag);
                        if (sub != null) applySub(itemObj, sub);
                    }
                } else if (itemObj.has("item")) {
                    String item = itemObj.get("item").getAsString();
                    String sub = resolveSub(category, item);
                    if ("minecraft:end_crystal".equals(item)) {
                        mat.addProperty("count", (int) END_CRYSTAL_COUNT);
                    }
                    if (sub != null) applySub(itemObj, sub);
                }
            }
        }
    }

    private static void applySub(JsonObject itemObj, String sub) {
        itemObj.remove("tag");
        itemObj.remove("item");
        if (sub.startsWith("tag:")) {
            itemObj.addProperty("tag", sub.substring(4));
        } else {
            itemObj.addProperty("item", sub.substring(5));
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
