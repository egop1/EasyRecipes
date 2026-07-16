package com.example.easyrecipes.net;

import com.example.easyrecipes.EasyRecipes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The deferred reload normally runs when the editor GUI closes ({@code FinishEditingPacket}), but
 * that packet never arrives if the editing player disconnects with the GUI open — alt-F4, crash,
 * kick. Their saved recipes would sit in the script unapplied, looking like the mod silently
 * failed. Flush on logout instead.
 */
@Mod.EventBusSubscriber(modid = EasyRecipes.MOD_ID)
public final class EditSessionEvents {

    private EditSessionEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerRecipes.reloadIfDirty(player);
        }
    }
}
