package com.example.easyrecipes.menu;

import com.example.easyrecipes.Registration;
import com.example.easyrecipes.Station;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Shared editor menu for the four furnace-family stations (input index 0 → output index 1). The
 * concrete station (smelting/blasting/smoking/campfire) is sent from the server via the open buffer.
 */
public class CookingMenu extends GhostMenu {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    private final int stationId;

    public CookingMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, buf.readVarInt(), buf.readUtf());
    }

    public CookingMenu(int id, Inventory playerInventory, int stationId, String editingId) {
        super(Registration.COOKING_MENU.get(), id, 2, editingId);
        this.stationId = stationId;
        addGhost(INPUT, 44, 34);
        addGhost(OUTPUT, 150, 36);
        addPlayerInventory(playerInventory, 22, 126);
    }

    public Station station() {
        return Station.byId(stationId);
    }

    public ItemStack getInput() {
        return ghostItem(INPUT);
    }

    public ItemStack getOutput() {
        return ghostItem(OUTPUT);
    }
}
