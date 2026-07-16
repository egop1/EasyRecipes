package com.example.easyrecipes.net;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.menu.CookingMenu;
import com.example.easyrecipes.menu.CraftingMenu;
import com.example.easyrecipes.menu.CreateProcessingMenu;
import com.example.easyrecipes.menu.DeployingMenu;
import com.example.easyrecipes.menu.EmptyingMenu;
import com.example.easyrecipes.menu.FillingMenu;
import com.example.easyrecipes.menu.MechanicalCraftingMenu;
import com.example.easyrecipes.menu.MixingMenu;
import com.example.easyrecipes.menu.SmithingMenu;
import com.example.easyrecipes.menu.StonecutterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Client → server: open a station's editor menu. {@code editingId} is the existing recipe id being
 * overridden ("" for a brand-new recipe); it is forwarded to the menu via the open buffer.
 */
public class OpenEditorPacket {

    private final int stationId;
    private final String editingId;
    private final int gridW;
    private final int gridH;

    public OpenEditorPacket(int stationId, String editingId) {
        this(stationId, editingId, 0, 0);
    }

    /** {@code gridW}/{@code gridH} are only used by the mechanical crafter (chosen before opening). */
    public OpenEditorPacket(int stationId, String editingId, int gridW, int gridH) {
        this.stationId = stationId;
        this.editingId = editingId == null ? "" : editingId;
        this.gridW = gridW;
        this.gridH = gridH;
    }

    public static void encode(OpenEditorPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.stationId);
        buf.writeUtf(msg.editingId);
        buf.writeVarInt(msg.gridW);
        buf.writeVarInt(msg.gridH);
    }

    public static OpenEditorPacket decode(FriendlyByteBuf buf) {
        return new OpenEditorPacket(buf.readVarInt(), buf.readUtf(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(OpenEditorPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Station station = Station.byId(msg.stationId);
            if (!station.available()) {
                return;
            }
            String edit = msg.editingId;
            Component title = Component.translatable("gui.easyrecipes.editor.title",
                    Component.translatable(station.translationKey()));

            switch (station) {
                case CRAFTING -> NetworkHooks.openScreen(player,
                        new SimpleMenuProvider((id, inv, p) -> new CraftingMenu(id, inv, edit), title),
                        buf -> buf.writeUtf(edit));
                case FURNACE, BLAST_FURNACE, SMOKER, CAMPFIRE, FAN_SMELTING, FAN_BLASTING -> {
                    int sid = station.ordinal();
                    NetworkHooks.openScreen(player,
                            new SimpleMenuProvider((id, inv, p) -> new CookingMenu(id, inv, sid, edit), title),
                            buf -> {
                                buf.writeVarInt(sid);
                                buf.writeUtf(edit);
                            });
                }
                case STONECUTTER -> NetworkHooks.openScreen(player,
                        new SimpleMenuProvider((id, inv, p) -> new StonecutterMenu(id, inv, edit), title),
                        buf -> buf.writeUtf(edit));
                case SMITHING -> NetworkHooks.openScreen(player,
                        new SimpleMenuProvider((id, inv, p) -> new SmithingMenu(id, inv, edit), title),
                        buf -> buf.writeUtf(edit));
                case CRUSHING, MILLING, PRESSING, CUTTING, SPLASHING, HAUNTING, SANDPAPER -> {
                    int sid = station.ordinal();
                    NetworkHooks.openScreen(player,
                            new SimpleMenuProvider((id, inv, p) -> new CreateProcessingMenu(id, inv, sid, edit), title),
                            buf -> {
                                buf.writeVarInt(sid);
                                buf.writeUtf(edit);
                            });
                }
                case DEPLOYING -> NetworkHooks.openScreen(player,
                        new SimpleMenuProvider((id, inv, p) -> new DeployingMenu(id, inv, edit), title),
                        buf -> buf.writeUtf(edit));
                case FILLING -> NetworkHooks.openScreen(player,
                        new SimpleMenuProvider((id, inv, p) -> new FillingMenu(id, inv, edit), title),
                        buf -> buf.writeUtf(edit));
                case EMPTYING -> NetworkHooks.openScreen(player,
                        new SimpleMenuProvider((id, inv, p) -> new EmptyingMenu(id, inv, edit), title),
                        buf -> buf.writeUtf(edit));
                case MIXING, COMPACTING -> {
                    int sid = station.ordinal();
                    NetworkHooks.openScreen(player,
                            new SimpleMenuProvider((id, inv, p) -> new MixingMenu(id, inv, sid, edit), title),
                            buf -> {
                                buf.writeVarInt(sid);
                                buf.writeUtf(edit);
                            });
                }
                case MECHANICAL_CRAFTING -> {
                    int w = Math.max(1, Math.min(9, msg.gridW));
                    int h = Math.max(1, Math.min(9, msg.gridH));
                    NetworkHooks.openScreen(player,
                            new SimpleMenuProvider((id, inv, p) -> new MechanicalCraftingMenu(id, inv, w, h, edit), title),
                            buf -> {
                                buf.writeVarInt(w);
                                buf.writeVarInt(h);
                                buf.writeUtf(edit);
                            });
                }
                default -> {
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
