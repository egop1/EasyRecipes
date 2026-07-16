package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.menu.StonecutterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Stonecutter editor: one input → one output (count supported). */
public class StonecutterScreen extends AbstractEditorScreen<StonecutterMenu> {

    public StonecutterScreen(StonecutterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected Station backStation() {
        return Station.STONECUTTER;
    }

    @Override
    protected RecipeEntry buildEntry() {
        ItemStack output = menu.getOutput();
        if (output.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return null;
        }
        ItemStack input = menu.getInput();
        if (input.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_input", GuiHelper.ERROR);
            return null;
        }
        return RecipeEntry.stonecutting(nameText(), itemId(input), itemId(output), outputCount());
    }
}
