package com.example.easyrecipes.net;

import com.example.easyrecipes.client.ClientRecipeCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: the authoritative recipe manifest, for the list screen. */
public class SyncRecipesPacket {

    private static final int MAX_JSON = 1048576;

    private final String json;

    public SyncRecipesPacket(String json) {
        this.json = json;
    }

    public static void encode(SyncRecipesPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, MAX_JSON);
    }

    public static SyncRecipesPacket decode(FriendlyByteBuf buf) {
        return new SyncRecipesPacket(buf.readUtf(MAX_JSON));
    }

    public static void handle(SyncRecipesPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRecipeCache.accept(msg.json)));
        ctx.setPacketHandled(true);
    }
}
