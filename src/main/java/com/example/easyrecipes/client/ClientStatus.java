package com.example.easyrecipes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Shows a server-pushed message on the recipe list, or in chat if the editor is already closed. */
public final class ClientStatus {

    private ClientStatus() {}

    public static void accept(String key, String detail) {
        Component message = detail == null || detail.isEmpty()
                ? Component.translatable(key)
                : Component.translatable(key, detail);

        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof RecipeListScreen list) {
            list.setServerStatus(message);
        } else if (Minecraft.getInstance().player != null) {
            // Saving returns to the list, so that is the usual case; this covers "closed it already".
            Minecraft.getInstance().player.sendSystemMessage(message);
        }
    }
}
