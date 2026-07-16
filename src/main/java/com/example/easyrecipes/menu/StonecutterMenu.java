package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Stonecutter editor: one ghost input (0) → one ghost output (1). */
public class StonecutterMenu extends GhostMenu {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    public StonecutterMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readUtf());
    }

    public StonecutterMenu(int id, Inventory playerInventory, String editingId) {
        super(Registration.STONECUTTER_MENU.get(), id, 2, editingId);
        addGhost(INPUT, 44, 34);
        addGhost(OUTPUT, 150, 36);
        addPlayerInventory(playerInventory, 22, 126);
    }

    public ItemStack getInput() {
        return ghostItem(INPUT);
    }

    public ItemStack getOutput() {
        return ghostItem(OUTPUT);
    }
}
