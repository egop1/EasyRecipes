package com.example.easyrecipes.net;

import com.example.easyrecipes.data.RecipeJson;
import com.example.easyrecipes.data.RecipeStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** Client → server: send me the current recipe manifest (the list screen was opened). */
public class RequestRecipesPacket {

    public RequestRecipesPacket() {}

    public static void encode(RequestRecipesPacket msg, FriendlyByteBuf buf) {}

    public static RequestRecipesPacket decode(FriendlyByteBuf buf) {
        return new RequestRecipesPacket();
    }

    public static void handle(RequestRecipesPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (!ServerRecipes.canEdit(player)) {
                ServerRecipes.fail(player, "gui.easyrecipes.error.no_permission", "");
                return;
            }
            Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncRecipesPacket(RecipeJson.listToJson(RecipeStore.load())));
        });
        ctx.setPacketHandled(true);
    }
}
