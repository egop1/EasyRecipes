package com.example.easyrecipes.client;

import com.example.easyrecipes.EasyRecipes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-side world lifecycle: drop the recipe cache so one server's list never shows on another. */
@Mod.EventBusSubscriber(modid = EasyRecipes.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {}

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientRecipeCache.clear();
    }
}
