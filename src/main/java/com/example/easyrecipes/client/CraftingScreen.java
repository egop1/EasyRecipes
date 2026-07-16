package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.menu.CraftingMenu;
import com.example.easyrecipes.script.CraftingScriptGenerator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Crafting-table editor: 3×3 ghost grid + output, with a shaped/shapeless toggle. */
public class CraftingScreen extends AbstractEditorScreen<CraftingMenu> {

    private Button toggleButton;
    private boolean shapeless = false;

    public CraftingScreen(CraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected Station backStation() {
        return Station.CRAFTING;
    }

    @Override
    protected void initExtra() {
        toggleButton = addRenderableWidget(Button.builder(toggleMessage(), b -> {
            shapeless = !shapeless;
            toggleButton.setMessage(toggleMessage());
        }).bounds(leftPos + 16, topPos + 82, 96, 18).build());
    }

    private Component toggleMessage() {
        return Component.translatable(shapeless
                ? "gui.easyrecipes.crafting.mode_shapeless"
                : "gui.easyrecipes.crafting.mode_shaped");
    }

    @Override
    protected RecipeEntry buildEntry() {
        ItemStack output = menu.getOutputItem();
        if (output.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return null;
        }
        String[] grid = new String[CraftingMenu.GRID];
        for (int i = 0; i < CraftingMenu.GRID; i++) {
            grid[i] = idOrEmpty(menu.getGridItem(i));
        }
        try {
            CraftingScriptGenerator.generate(nameText(), grid, itemId(output), outputCount(), shapeless);
        } catch (IllegalArgumentException e) {
            setStatus("gui.easyrecipes.crafting.no_input", GuiHelper.ERROR);
            return null;
        }
        return RecipeEntry.crafting(nameText(), grid, itemId(output), outputCount(), shapeless);
    }
}
