package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 3×3 ghost grid (indices 0..8) + one ghost output (index 9). */
public class CraftingMenu extends GhostMenu {

    public static final int GRID = 9;
    public static final int OUTPUT_INDEX = 9;

    public CraftingMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readUtf());
    }

    public CraftingMenu(int id, Inventory playerInventory, String editingId) {
        super(Registration.CRAFTING_MENU.get(), id, GRID + 1, editingId);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addGhost(row * 3 + col, 16 + col * 18, 18 + row * 18);
            }
        }
        addGhost(OUTPUT_INDEX, 150, 36);
        addPlayerInventory(playerInventory, 22, 126);
    }

    public ItemStack getGridItem(int index) {
        return ghostItem(index);
    }

    public ItemStack getOutputItem() {
        return ghostItem(OUTPUT_INDEX);
    }
}
