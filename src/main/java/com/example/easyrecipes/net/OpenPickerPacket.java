package com.example.easyrecipes.net;

import com.example.easyrecipes.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: open the station picker screen (triggered by {@code /easyrecipes}). */
public class OpenPickerPacket {

    public OpenPickerPacket() {}

    public static void encode(OpenPickerPacket msg, FriendlyByteBuf buf) {}

    public static OpenPickerPacket decode(FriendlyByteBuf buf) {
        return new OpenPickerPacket();
    }

    public static void handle(OpenPickerPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandler::openPicker));
        ctx.setPacketHandled(true);
    }
}
