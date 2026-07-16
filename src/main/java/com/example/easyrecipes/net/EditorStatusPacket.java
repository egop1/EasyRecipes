package com.example.easyrecipes.net;

import com.example.easyrecipes.client.ClientStatus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → client: something the player must know about (a refused or failed edit). The client
 * writes the files through the server now, so without this a failed save would look identical to a
 * successful one.
 */
public class EditorStatusPacket {

    private static final int MAX_DETAIL = 512;

    private final String key;
    private final String detail;

    public EditorStatusPacket(String key, String detail) {
        this.key = key;
        this.detail = detail == null ? "" : detail;
    }

    public static void encode(EditorStatusPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.key);
        buf.writeUtf(msg.detail, MAX_DETAIL);
    }

    public static EditorStatusPacket decode(FriendlyByteBuf buf) {
        return new EditorStatusPacket(buf.readUtf(), buf.readUtf(MAX_DETAIL));
    }

    public static void handle(EditorStatusPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientStatus.accept(msg.key, msg.detail)));
        ctx.setPacketHandled(true);
    }
}
