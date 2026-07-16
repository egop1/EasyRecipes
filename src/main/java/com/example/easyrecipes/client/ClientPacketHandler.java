package com.example.easyrecipes.client;

import net.minecraft.client.Minecraft;

/** Client-only packet reactions. Referenced only through {@link net.minecraftforge.fml.DistExecutor}. */
public final class ClientPacketHandler {

    private ClientPacketHandler() {}

    public static void openPicker() {
        Minecraft.getInstance().setScreen(new CategorySelectScreen());
    }
}
