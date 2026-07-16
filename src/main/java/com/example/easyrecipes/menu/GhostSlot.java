package com.example.easyrecipes.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A display-only slot. The player cannot insert or take items normally; its contents are set by
 * the owning menu's {@code clicked} override, which copies (does not consume) the carried stack.
 */
public class GhostSlot extends Slot {

    public GhostSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
