package com.example.easyrecipes.net;

import com.example.easyrecipes.EasyRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/** The mod's network channel and packet registration. */
public final class Network {

    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    private Network() {}

    /**
     * Tells the server the editor is closed so it can run the reload that saving defers.
     * Every editor screen calls this from {@code onClose()} — which fires on Esc/close only, not
     * when we swap between our own screens. Harmless when nothing changed.
     */
    public static void finishEditing() {
        CHANNEL.sendToServer(new FinishEditingPacket());
    }

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(EasyRecipes.MOD_ID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

        CHANNEL.registerMessage(0, OpenPickerPacket.class,
                OpenPickerPacket::encode, OpenPickerPacket::decode, OpenPickerPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(1, OpenEditorPacket.class,
                OpenEditorPacket::encode, OpenEditorPacket::decode, OpenEditorPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(2, SaveRecipePacket.class,
                SaveRecipePacket::encode, SaveRecipePacket::decode, SaveRecipePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(3, RemoveRecipePacket.class,
                RemoveRecipePacket::encode, RemoveRecipePacket::decode, RemoveRecipePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(4, RequestRecipesPacket.class,
                RequestRecipesPacket::encode, RequestRecipesPacket::decode, RequestRecipesPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(5, SyncRecipesPacket.class,
                SyncRecipesPacket::encode, SyncRecipesPacket::decode, SyncRecipesPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(6, FinishEditingPacket.class,
                FinishEditingPacket::encode, FinishEditingPacket::decode, FinishEditingPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(7, EditorStatusPacket.class,
                EditorStatusPacket::encode, EditorStatusPacket::decode, EditorStatusPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
