package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import com.example.easyrecipes.Station;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Shared editor for mixing and compacting: up to three item inputs (0..2) + one input fluid (3) →
 * one output item (4) or one output fluid (5); plus a heat requirement chosen in the screen.
 */
public class MixingMenu extends GhostMenu {

    public static final int IN0 = 0;
    public static final int IN1 = 1;
    public static final int IN2 = 2;
    public static final int FLUID_IN = 3;
    public static final int OUT = 4;
    public static final int FLUID_OUT = 5;

    private final int stationId;

    public MixingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readVarInt(), buf.readUtf());
    }

    public MixingMenu(int id, Inventory inv, int stationId, String editingId) {
        super(Registration.MIXING_MENU.get(), id, 6, editingId);
        this.stationId = stationId;
        // Rows are LABELLED_ROW (29) apart: each slot has a caption above it, and at the old
        // 24px spacing the caption had 6px to live in and sat on both slots at once.
        // The right column starts at 142 so that "slot + mB field" ends on the 16px gutter.
        addGhost(IN0, 16, 30);
        addGhost(IN1, 34, 30);
        addGhost(IN2, 52, 30);
        addGhost(FLUID_IN, 16, 60);
        addGhost(OUT, 142, 30);
        addGhost(FLUID_OUT, 142, 60);
        addPlayerInventory(inv, 22, 126);
    }

    @Override
    protected int ghostCopyCount(int index, ItemStack carried) {
        return index == OUT ? carried.getCount() : 1;
    }

    public Station station() {
        return Station.byId(stationId);
    }

    public ItemStack itemInput(int i) {
        return ghostItem(IN0 + i);
    }

    public ItemStack fluidIn() {
        return ghostItem(FLUID_IN);
    }

    public ItemStack outputItem() {
        return ghostItem(OUT);
    }

    public ItemStack fluidOut() {
        return ghostItem(FLUID_OUT);
    }
}
