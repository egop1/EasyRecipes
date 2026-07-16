package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Spout filling editor: container item (0) + input fluid bucket (1) → filled item (2). */
public class FillingMenu extends GhostMenu {

    public static final int CONTAINER = 0;
    public static final int FLUID = 1;
    public static final int OUTPUT = 2;

    public FillingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readUtf());
    }

    public FillingMenu(int id, Inventory inv, String editingId) {
        super(Registration.FILLING_MENU.get(), id, 3, editingId);
        // 30 -> 60 keeps LABELLED_ROW between the two captioned slots; at 26 apart the
        // "fluid" caption had nowhere to sit and overlapped both.
        addGhost(CONTAINER, 44, 30);
        addGhost(FLUID, 44, 60);
        addGhost(OUTPUT, 150, 36);
        addPlayerInventory(inv, 22, 126);
    }

    @Override
    protected int ghostCopyCount(int index, ItemStack carried) {
        return index == OUTPUT ? carried.getCount() : 1;
    }

    public ItemStack container() {
        return ghostItem(CONTAINER);
    }

    public ItemStack fluidBucket() {
        return ghostItem(FLUID);
    }

    public ItemStack output() {
        return ghostItem(OUTPUT);
    }
}
