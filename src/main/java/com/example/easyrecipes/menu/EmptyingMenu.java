package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Item Drain emptying editor: input item (0) → output item (1) + output fluid bucket (2). */
public class EmptyingMenu extends GhostMenu {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int FLUID = 2;

    public EmptyingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readUtf());
    }

    public EmptyingMenu(int id, Inventory inv, String editingId) {
        super(Registration.EMPTYING_MENU.get(), id, 3, editingId);
        // Right column starts at 142 so "slot + mB field" ends on the 16px gutter; the two
        // captioned slots are LABELLED_ROW apart so their captions have room.
        addGhost(INPUT, 44, 34);
        addGhost(OUTPUT, 142, 30);
        addGhost(FLUID, 142, 60);
        addPlayerInventory(inv, 22, 126);
    }

    @Override
    protected int ghostCopyCount(int index, ItemStack carried) {
        return index == OUTPUT ? carried.getCount() : 1;
    }

    public ItemStack input() {
        return ghostItem(INPUT);
    }

    public ItemStack output() {
        return ghostItem(OUTPUT);
    }

    public ItemStack fluidBucket() {
        return ghostItem(FLUID);
    }
}
