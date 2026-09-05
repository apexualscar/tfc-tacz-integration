package com.tacz.tfcintegration.recipe;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.tacz.tfcintegration.config.ModConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

public class GunDataGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String GUNPACK_NAME = "zz_tacz_tfc_integration";

    // Gun damage multiplier per tier: t1=30% ... t6=100%.
    private static final double[] DAMAGE_SCALE = {0.30, 0.44, 0.58, 0.72, 0.86, 1.00};
    // Point-blank damage floor for 12g so tier-1 shotguns still one-shot a spider (16 HP).
    private static final double SHOTGUN_FLOOR = 16.0;
    private static final String SHOTGUN_AMMO = "tacz:12g";

    public static int generate() {
        Map<String, String> tiers = ModConfig.getRecipeTiers();
        Path gunpackDir = FMLPaths.GAMEDIR.get().resolve("tacz").resolve(GUNPACK_NAME);
        Path outDir = gunpackDir.resolve("data").resolve("tacz").resolve("data").resolve("guns");

        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create gun data output dir", e);
            return 0;
        }

        int count = 0;
        for (Map.Entry<String, String> entry : tiers.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("guns/")) continue;
            String gunId = key.substring("guns/".length());
            String tier = entry.getValue();

            JsonObject data = readDefaultGunData(gunId);
            if (data == null) {
                LOGGER.warn("Default gun data not found: {}", gunId);
                continue;
            }
            scaleDamage(data, tier);
            try {
                Files.writeString(outDir.resolve(gunId + "_data.json"), GSON.toJson(data));
                count++;
            } catch (IOException e) {
                LOGGER.debug("Failed to write gun data {}: {}", gunId, e.getMessage());
            }
        }
        LOGGER.info("Generated {} gun data overrides", count);
        return count;
    }

    private static void scaleDamage(JsonObject data, String tier) {
        int idx = tierIndex(tier);
        if (idx < 0) return;
        double mult = DAMAGE_SCALE[idx];

        JsonObject bullet = data.has("bullet") ? data.getAsJsonObject("bullet") : null;
        if (bullet == null) return;

        if (SHOTGUN_AMMO.equals(getAmmo(data)) && bullet.has("damage")) {
            double base = bullet.get("damage").getAsDouble();
            mult = Math.max(mult, SHOTGUN_FLOOR / base);
        }

        scaleFields(bullet, mult);
        if (bullet.has("extra_damage")) {
            JsonObject extra = bullet.getAsJsonObject("extra_damage");
            if (extra.has("damage_adjust")) {
                for (JsonElement e : extra.getAsJsonArray("damage_adjust")) {
                    if (e.isJsonObject()) scaleFields(e.getAsJsonObject(), mult);
                }
            }
        }
        if (bullet.has("explosion")) {
            scaleFields(bullet.getAsJsonObject("explosion"), mult);
        }
        if (data.has("fire_mode_adjust")) {
            for (Map.Entry<String, JsonElement> e : data.getAsJsonObject("fire_mode_adjust").entrySet()) {
                if (e.getValue().isJsonObject()) scaleFields(e.getValue().getAsJsonObject(), mult);
            }
        }
        if (data.has("melee")) {
            JsonObject melee = data.getAsJsonObject("melee");
            if (melee.has("default") && melee.get("default").isJsonObject()) {
                scaleFields(melee.getAsJsonObject("default"), mult);
            }
        }
    }

    private static String getAmmo(JsonObject data) {
        JsonElement ammo = data.get("ammo");
        return ammo != null && ammo.isJsonPrimitive() ? ammo.getAsString() : "";
    }

    private static void scaleFields(JsonObject obj, double mult) {
        if (obj.has("damage") && obj.get("damage").isJsonPrimitive()) {
            double v = obj.get("damage").getAsDouble() * mult;
            obj.addProperty("damage", Math.round(v * 10.0) / 10.0);
        }
    }

    private static int tierIndex(String tier) {
        if (tier.startsWith("t") && tier.length() == 2) {
            int n = tier.charAt(1) - '0';
            if (n >= 1 && n <= 6) return n - 1;
        }
        return -1;
    }

    private static JsonObject readDefaultGunData(String gunId) {
        String fileName = gunId + "_data.json";
        Path unpacked = FMLPaths.GAMEDIR.get().resolve("tacz").resolve("tacz_default_gun")
            .resolve("data").resolve("tacz").resolve("data").resolve("guns").resolve(fileName);
        JsonObject recipe = readJson(unpacked);
        if (recipe != null) return recipe;

        var modFile = ModList.get().getModFileById("tacz");
        if (modFile != null) {
            for (String prefix : new String[]{
                "assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/",
                "data/tacz/data/guns/"
            }) {
                var resource = modFile.getFile().findResource(prefix + fileName);
                if (resource != null) {
                    try (InputStream in = Files.newInputStream(resource)) {
                        return parse(in);
                    } catch (Exception e) {
                        LOGGER.debug("Failed to read default gun data {}: {}", gunId, e.getMessage());
                        return null;
                    }
                }
            }
        }

        return null;
    }

    private static JsonObject readJson(Path path) {
        if (!Files.exists(path)) return null;
        try (InputStream in = Files.newInputStream(path)) {
            return parse(in);
        } catch (Exception e) {
            LOGGER.debug("Failed to read gun data {}: {}", path, e.getMessage());
            return null;
        }
    }

    // TACZ gun data files carry // and /* */ comments; strip them before parsing.
    private static JsonObject parse(InputStream in) throws IOException {
        String src = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        return JsonParser.parseString(stripComments(src)).getAsJsonObject();
    }

    private static String stripComments(String src) {
        StringBuilder sb = new StringBuilder(src.length());
        boolean inString = false, inLine = false, inBlock = false;
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            char next = i + 1 < src.length() ? src.charAt(i + 1) : 0;
            if (inLine) {
                if (c == '\n') { inLine = false; sb.append(c); }
                continue;
            }
            if (inBlock) {
                if (c == '*' && next == '/') { inBlock = false; i++; }
                continue;
            }
            if (inString) {
                sb.append(c);
                if (c == '\\') { sb.append(next); i++; }
                else if (c == '"') inString = false;
                continue;
            }
            if (c == '"') { inString = true; sb.append(c); continue; }
            if (c == '/' && next == '/') { inLine = true; i++; continue; }
            if (c == '/' && next == '*') { inBlock = true; i++; continue; }
            sb.append(c);
        }
        return sb.toString();
    }
}