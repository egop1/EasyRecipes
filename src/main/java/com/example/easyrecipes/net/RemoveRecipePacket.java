package com.example.easyrecipes.net;

import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.data.RecipeStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** Client → server: remove the recipe with this id. */
public class RemoveRecipePacket {

    private final String id;

    public RemoveRecipePacket(String id) {
        this.id = id == null ? "" : id;
    }

    public static void encode(RemoveRecipePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.id);
    }

    public static RemoveRecipePacket decode(FriendlyByteBuf buf) {
        return new RemoveRecipePacket(buf.readUtf());
    }

    public static void handle(RemoveRecipePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (!ServerRecipes.canEdit(player)) {
                ServerRecipes.fail(player, "gui.easyrecipes.error.no_permission", "");
                return;
            }
            if (msg.id.isEmpty()) {
                return;
            }
            try {
                List<RecipeEntry> all = RecipeStore.load();
                boolean removed = all.removeIf(entry -> msg.id.equals(entry.id));
                if (removed) {
                    RecipeStore.saveAndRegenerate(all);
                }
                ServerRecipes.markDirtyAndSync(player, all);
            } catch (Exception e) {
                com.example.easyrecipes.EasyRecipes.LOGGER.error("Failed to remove recipe", e);
                ServerRecipes.fail(player, "gui.easyrecipes.error.remove_failed", String.valueOf(e.getMessage()));
            }
        });
        ctx.setPacketHandled(true);
    }
}
