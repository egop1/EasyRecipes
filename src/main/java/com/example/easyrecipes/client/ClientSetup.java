package com.example.easyrecipes.client;

import com.example.easyrecipes.EasyRecipes;
import com.example.easyrecipes.Registration;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only wiring: binds each editor menu to its screen. Picker/list are plain screens. */
@Mod.EventBusSubscriber(modid = EasyRecipes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Registration.CRAFTING_MENU.get(), CraftingScreen::new);
            MenuScreens.register(Registration.COOKING_MENU.get(), CookingScreen::new);
            MenuScreens.register(Registration.STONECUTTER_MENU.get(), StonecutterScreen::new);
            MenuScreens.register(Registration.SMITHING_MENU.get(), SmithingScreen::new);
            MenuScreens.register(Registration.CREATE_PROCESSING_MENU.get(), CreateProcessingScreen::new);
            MenuScreens.register(Registration.DEPLOYING_MENU.get(), DeployingScreen::new);
            MenuScreens.register(Registration.FILLING_MENU.get(), FillingScreen::new);
            MenuScreens.register(Registration.EMPTYING_MENU.get(), EmptyingScreen::new);
            MenuScreens.register(Registration.MIXING_MENU.get(), MixingScreen::new);
            MenuScreens.register(Registration.MECHANICAL_CRAFTING_MENU.get(), MechanicalCraftingScreen::new);
        });
    }
}
