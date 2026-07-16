package com.example.easyrecipes;

import com.example.easyrecipes.net.Network;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Easy Recipes — an in-game visual recipe editor that generates KubeJS scripts. Run
 * {@code /easyrecipes} to open the station picker. The MVP wires up the crafting table.
 */
@Mod(EasyRecipes.MOD_ID)
public class EasyRecipes {

    public static final String MOD_ID = "easyrecipes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EasyRecipes() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.register(modBus);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Network::register);
    }
}
