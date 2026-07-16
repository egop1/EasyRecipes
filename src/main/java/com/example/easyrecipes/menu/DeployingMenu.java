package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Deployer editor: base item (0) + held item (1) → one output (2). */
public class DeployingMenu extends GhostMenu {

    public static final int BASE = 0;
    public static final int HELD = 1;
    public static final int OUTPUT = 2;

    public DeployingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readUtf());
    }

    public DeployingMenu(int id, Inventory inv, String editingId) {
        super(Registration.DEPLOYING_MENU.get(), id, 3, editingId);
        addGhost(BASE, 16, 30);
        addGhost(HELD, 38, 30);
        addGhost(OUTPUT, 150, 36);
        addPlayerInventory(inv, 22, 126);
    }

    @Override
    protected int ghostCopyCount(int index, ItemStack carried) {
        return index == OUTPUT ? carried.getCount() : 1;
    }

    public ItemStack base() {
        return ghostItem(BASE);
    }

    public ItemStack held() {
        return ghostItem(HELD);
    }

    public ItemStack output() {
        return ghostItem(OUTPUT);
    }
}
