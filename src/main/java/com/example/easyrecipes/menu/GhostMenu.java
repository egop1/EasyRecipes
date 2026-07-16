package com.example.easyrecipes.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Base for all editor menus: a set of ghost slots (indices {@code 0..ghostSize-1}) that copy the
 * carried item without consuming it, plus the player inventory. Subclasses add the ghost slots in
 * their own layout, then call {@link #addPlayerInventory}.
 */
public abstract class GhostMenu extends AbstractContainerMenu {

    protected final Container ghost;
    private final int ghostSize;
    private final String editingId;

    protected GhostMenu(MenuType<?> type, int id, int ghostSize, String editingId) {
        super(type, id);
        this.ghostSize = ghostSize;
        this.ghost = new SimpleContainer(ghostSize);
        this.editingId = editingId == null ? "" : editingId;
    }

    /** The existing recipe id this editor is overriding, or "" for a brand-new recipe. */
    public String editingId() {
        return editingId;
    }

    protected void addGhost(int index, int x, int y) {
        addSlot(new GhostSlot(ghost, index, x, y));
    }

    protected void addPlayerInventory(Inventory inventory, int originX, int originY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, originX + col * 18, originY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, originX + col * 18, originY + 58));
        }
    }

    public ItemStack ghostItem(int index) {
        return ghost.getItem(index);
    }

    /** How many items to store when copying the carried stack onto ghost slot {@code index}. */
    protected int ghostCopyCount(int index, ItemStack carried) {
        return 1;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < ghostSize) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                ghost.setItem(slotId, ItemStack.EMPTY);
            } else {
                ItemStack copy = carried.copy();
                copy.setCount(Math.max(1, ghostCopyCount(slotId, carried)));
                ghost.setItem(slotId, copy);
            }
            return;
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
