package com.example.easyrecipes.net;

import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.data.RecipeJson;
import com.example.easyrecipes.data.RecipeStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Client → server: add a recipe. The server owns the files, so it does the writing and reloading. */
public class SaveRecipePacket {

    private static final int MAX_JSON = 32767;

    private final String json;

    public SaveRecipePacket(String json) {
        this.json = json;
    }

    public static void encode(SaveRecipePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, MAX_JSON);
    }

    public static SaveRecipePacket decode(FriendlyByteBuf buf) {
        return new SaveRecipePacket(buf.readUtf(MAX_JSON));
    }

    public static void handle(SaveRecipePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (!ServerRecipes.canEdit(player)) {
                ServerRecipes.fail(player, "gui.easyrecipes.error.no_permission", "");
                return;
            }
            try {
                RecipeEntry entry = RecipeJson.fromJson(msg.json);
                if (entry == null || entry.station == null) {
                    ServerRecipes.fail(player, "gui.easyrecipes.error.save_failed", "bad recipe data");
                    return;
                }
                if (entry.id == null || entry.id.isBlank()) {
                    entry.id = UUID.randomUUID().toString();
                }
                // Generate once up front: the manifest is written before the script is rebuilt, so
                // an unusable entry would be persisted and then silently skipped forever.
                com.example.easyrecipes.script.RecipeScriptGenerator.generate(entry);

                List<RecipeEntry> all = RecipeStore.load();
                // An entry with `editing` overrides that one recipe id, so a second override of the
                // same id must replace the first — otherwise both variants stay craftable, and a
                // later deletion of the same id would fight the earlier edit. Brand-new recipes
                // (no `editing`) are never deduplicated: several of them are perfectly valid.
                if (entry.editing != null && !entry.editing.isBlank()) {
                    all.removeIf(existing -> entry.editing.equals(existing.editing));
                }
                all.add(entry);
                RecipeStore.saveAndRegenerate(all);
                ServerRecipes.markDirtyAndSync(player, all);
            } catch (Exception e) {
                com.example.easyrecipes.EasyRecipes.LOGGER.error("Failed to save recipe", e);
                ServerRecipes.fail(player, "gui.easyrecipes.error.save_failed", String.valueOf(e.getMessage()));
            }
        });
        ctx.setPacketHandled(true);
    }
}
