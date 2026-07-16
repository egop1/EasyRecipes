package com.example.easyrecipes.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server: the editor GUI was closed. Triggers the datapack reload that saving deliberately
 * skips, so a session of edits costs one reload instead of one per Save.
 *
 * <p>Carries no payload and is a no-op when nothing was changed, so screens can fire it on every
 * close without checking anything.
 */
public class FinishEditingPacket {

    public FinishEditingPacket() {}

    public static void encode(FinishEditingPacket msg, FriendlyByteBuf buf) {
        // nothing to send: the fact of the close is the whole message
    }

    public static FinishEditingPacket decode(FriendlyByteBuf buf) {
        return new FinishEditingPacket();
    }

    public static void handle(FinishEditingPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (!ServerRecipes.canEdit(player)) {
                return;
            }
            ServerRecipes.reloadIfDirty(player);
        });
        ctx.setPacketHandled(true);
    }
}
