package com.tacz.tfcintegration.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("tacz-tfc-integration");

    private static Map<String, String> tierMetals = new HashMap<>();
    private static Map<String, String> recipeTiers = new HashMap<>();
    private static Map<String, String> blockRecipeTiers = new HashMap<>();

    public static void load() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }
        copyDefaultsIfNeeded();
        loadTierMetals();
        loadRecipeTiers();
        loadBlockRecipeTiers();
    }

    private static void copyDefaultsIfNeeded() {
        copyIfMissing("tier-metals.toml");
        copyIfMissing("recipe-tiers.toml");
    }

    private static void copyIfMissing(String fileName) {
        Path target = CONFIG_DIR.resolve(fileName);
        if (!Files.exists(target) || !defaultMatches(fileName, target)) {
            try (InputStream in = ModConfig.class.getResourceAsStream("/default_config/" + fileName)) {
                if (in != null) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to write default config: {}", fileName, e);
            }
        }
    }

    private static boolean defaultMatches(String fileName, Path target) {
        try (InputStream in = ModConfig.class.getResourceAsStream("/default_config/" + fileName)) {
            if (in == null) return true;
            return Arrays.equals(in.readAllBytes(), Files.readAllBytes(target));
        } catch (IOException e) {
            return false;
        }
    }

    private static void loadTierMetals() {
        tierMetals.clear();
        Path file = CONFIG_DIR.resolve("tier-metals.toml");
        if (!Files.exists(file)) return;

        String currentTier = null;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.equals("[tier_metals]")) continue;

                if (line.startsWith("[")) {
                    // next section, stop
                    break;
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replace("\"", "");
                    tierMetals.put(key, value);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read tier-metals.toml", e);
        }
        LOGGER.info("Loaded {} tier metals", tierMetals.size());
    }

    private static void loadRecipeTiers() {
        recipeTiers.clear();
        Path file = CONFIG_DIR.resolve("recipe-tiers.toml");
        if (!Files.exists(file)) return;

        String currentSection = null;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("[recipe_tiers.")) {
                    currentSection = line.substring(14, line.length() - 1);
                    continue;
                }

                if (line.startsWith("[block_recipes]")) {
                    currentSection = "block";
                    continue;
                }

                if (line.startsWith("[")) {
                    currentSection = null;
                    continue;
                }

                if (currentSection == null) continue;

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim().replace("\"", "");
                    String value = parts[1].trim().replace("\"", "");

                    if ("block".equals(currentSection)) {
                        blockRecipeTiers.put(key, value);
                    } else {
                        String prefix = currentSection + "/";
                        recipeTiers.put(prefix + key, value);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read recipe-tiers.toml", e);
        }
        LOGGER.info("Loaded {} recipe tiers, {} block recipe tiers", recipeTiers.size(), blockRecipeTiers.size());
    }

    private static void loadBlockRecipeTiers() {
        // block tiers loaded together with recipe tiers
    }

    public static String getMetalForTier(String tier) {
        return tierMetals.getOrDefault(tier, "tfc:metal/ingot/wrought_iron");
    }

    public static String getTierForRecipe(String category, String recipeId) {
        String key = category + "/" + recipeId;
        return recipeTiers.get(key);
    }

    public static String getTierForBlockRecipe(String recipeId) {
        return blockRecipeTiers.getOrDefault(recipeId, "t1");
    }

    public static Map<String, String> getTierMetals() {
        return tierMetals;
    }

    public static Map<String, String> getRecipeTiers() {
        return recipeTiers;
    }

    public static Map<String, String> getBlockRecipeTiers() {
        return blockRecipeTiers;
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }
}
