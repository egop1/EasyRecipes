package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Mechanical crafter editor of an arbitrary grid size. The size is chosen before opening (sent in
 * the open buffer), so the ghost slots can be laid out to fit exactly: grid cells are indices
 * {@code 0..gridW*gridH-1} (row-major), the output is the last index.
 */
public class MechanicalCraftingMenu extends GhostMenu {

    public static final int GRID_X = 8;
    public static final int GRID_Y = 18;
    private static final int INV_W = 162;

    private final int gridW;
    private final int gridH;

    public MechanicalCraftingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readVarInt(), buf.readVarInt(), buf.readUtf());
    }

    public MechanicalCraftingMenu(int id, Inventory inv, int gridW, int gridH, String editingId) {
        super(Registration.MECHANICAL_CRAFTING_MENU.get(), id, gridW * gridH + 1, editingId);
        this.gridW = gridW;
        this.gridH = gridH;

        for (int r = 0; r < gridH; r++) {
            for (int c = 0; c < gridW; c++) {
                addGhost(r * gridW + c, GRID_X + c * 18, GRID_Y + r * 18);
            }
        }
        addGhost(outputIndex(), outputX(gridW), outputY(gridH));
        addPlayerInventory(inv, invX(gridW), invY(gridH));
    }

    public static int panelWidth(int gridW) {
        return Math.max(244, GRID_X + gridW * 18 + 48);
    }

    public static int panelHeight(int gridH) {
        return GRID_Y + gridH * 18 + 144;
    }

    public static int outputX(int gridW) {
        return GRID_X + gridW * 18 + 22;
    }

    public static int outputY(int gridH) {
        return GRID_Y + (gridH * 18) / 2 - 9;
    }

    public static int invX(int gridW) {
        return (panelWidth(gridW) - INV_W) / 2;
    }

    public static int invY(int gridH) {
        return GRID_Y + gridH * 18 + 52;
    }

    public int gridW() {
        return gridW;
    }

    public int gridH() {
        return gridH;
    }

    public int outputIndex() {
        return gridW * gridH;
    }

    @Override
    protected int ghostCopyCount(int index, ItemStack carried) {
        return index == outputIndex() ? carried.getCount() : 1;
    }

    public ItemStack gridItem(int index) {
        return ghostItem(index);
    }

    public ItemStack output() {
        return ghostItem(outputIndex());
    }
}
