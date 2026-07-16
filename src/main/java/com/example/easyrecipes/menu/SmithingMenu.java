package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Smithing table editor: template (0), base (1), addition (2) → output (3). */
public class SmithingMenu extends GhostMenu {

    public static final int TEMPLATE = 0;
    public static final int BASE = 1;
    public static final int ADDITION = 2;
    public static final int OUTPUT = 3;

    public SmithingMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readUtf());
    }

    public SmithingMenu(int id, Inventory playerInventory, String editingId) {
        super(Registration.SMITHING_MENU.get(), id, 4, editingId);
        addGhost(TEMPLATE, 30, 34);
        addGhost(BASE, 52, 34);
        addGhost(ADDITION, 74, 34);
        addGhost(OUTPUT, 150, 36);
        addPlayerInventory(playerInventory, 22, 126);
    }

    public ItemStack get(int index) {
        return ghostItem(index);
    }
}
