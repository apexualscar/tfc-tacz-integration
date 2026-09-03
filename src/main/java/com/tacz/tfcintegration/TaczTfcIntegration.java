package com.tacz.tfcintegration;

import com.tacz.tfcintegration.config.ModConfig;
import com.tacz.tfcintegration.recipe.BlockRecipeGenerator;
import com.tacz.tfcintegration.recipe.GunpackGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(TaczTfcIntegration.MOD_ID)
public class TaczTfcIntegration {
    public static final String MOD_ID = "tacz_tfc_integration";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TaczTfcIntegration() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModConfig.load();
            LOGGER.info("TACZ TFC Integration: generating recipes");
            GunpackGenerator.generate();
            BlockRecipeGenerator.generate();
        });
    }
}
