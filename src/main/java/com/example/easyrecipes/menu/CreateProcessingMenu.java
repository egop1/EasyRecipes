package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import com.example.easyrecipes.Station;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Editor menu for the Create machines: one input (0) and one output (1). Crushing/milling use the
 * output as a "staging" slot the screen commits into a weighted list; pressing/cutting use it as
 * the single output. The output slot keeps the dragged stack's count.
 */
public class CreateProcessingMenu extends GhostMenu {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    private final int stationId;

    public CreateProcessingMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readVarInt(), buf.readUtf());
    }

    public CreateProcessingMenu(int id, Inventory playerInventory, int stationId, String editingId) {
        super(Registration.CREATE_PROCESSING_MENU.get(), id, 2, editingId);
        this.stationId = stationId;
        Station station = Station.byId(stationId);
        boolean weighted = station.weightedOutputs();
        if (weighted) {
            addGhost(INPUT, 16, 30);
            addGhost(OUTPUT, 56, 30);
        } else {
            addGhost(INPUT, 44, 34);
            addGhost(OUTPUT, 150, 36);
        }
        // Weighted needs a full-width strip for the growing outputs list, so its panel is taller
        // (see CreateProcessingScreen) and the inventory sits lower to match.
        addPlayerInventory(playerInventory, 22, weighted ? 160 : 126);
    }

    @Override
    protected int ghostCopyCount(int index, ItemStack carried) {
        return index == INPUT ? 1 : carried.getCount();
    }

    public Station station() {
        return Station.byId(stationId);
    }

    public ItemStack getInput() {
        return ghostItem(INPUT);
    }

    public ItemStack getOutput() {
        return ghostItem(OUTPUT);
    }
}
