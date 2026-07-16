package com.example.easyrecipes.net;

import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.data.RecipeJson;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/** Server-side helpers shared by the recipe packets. */
final class ServerRecipes {

    /**
     * Set by every save/remove, cleared by the reload. Server-wide rather than per-player because
     * the recipes themselves are: whoever closes their editor first reloads for everyone.
     */
    private static boolean dirty = false;

    private ServerRecipes() {}

    /** Editing recipes changes server-wide data, so require operator level. */
    static boolean canEdit(ServerPlayer player) {
        return player != null && player.hasPermissions(2);
    }

    /**
     * Tells the player an edit was refused or failed. The client no longer writes anything itself,
     * so without this a failure is invisible and looks exactly like success.
     */
    static void fail(ServerPlayer player, String key, String detail) {
        if (player == null) {
            return;
        }
        Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new EditorStatusPacket(key, detail));
    }

    /**
     * Records that the script changed and sends the manifest back to the editing player.
     *
     * <p>Deliberately does <em>not</em> reload: "/reload" rebuilds every datapack and costs seconds,
     * and firing it per Save made a five-recipe session five full reloads. The reload is deferred to
     * {@link #reloadIfDirty} when the editor closes — the price is that a saved recipe is not live
     * in the world until you leave the GUI.
     */
    static void markDirtyAndSync(ServerPlayer player, List<RecipeEntry> all) {
        dirty = true;
        Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncRecipesPacket(RecipeJson.listToJson(all)));
    }

    /**
     * Reloads datapacks so KubeJS re-runs the regenerated script (vanilla then pushes the updated
     * recipes to every connected client). No-op when nothing changed, so it is safe to call on
     * every editor close.
     */
    static void reloadIfDirty(ServerPlayer player) {
        if (!dirty) {
            return;
        }
        dirty = false;
        MinecraftServer server = player.getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "reload");
        }
    }
}
