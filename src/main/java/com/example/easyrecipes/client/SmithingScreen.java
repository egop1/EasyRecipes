package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.menu.SmithingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Smithing table editor (transform): template + base + addition → output. Template is optional. */
public class SmithingScreen extends AbstractEditorScreen<SmithingMenu> {

    public SmithingScreen(SmithingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected Station backStation() {
        return Station.SMITHING;
    }

    @Override
    protected boolean hasCountBox() {
        return false;
    }

    @Override
    protected void renderExtras(GuiGraphics g) {
        // Labels above the three input slots (template / base / addition).
        g.drawString(font, "T", leftPos + 34, topPos + 24, GuiHelper.TITLE, false);
        g.drawString(font, "B", leftPos + 56, topPos + 24, GuiHelper.TITLE, false);
        g.drawString(font, "A", leftPos + 78, topPos + 24, GuiHelper.TITLE, false);
    }

    @Override
    protected RecipeEntry buildEntry() {
        ItemStack output = menu.get(SmithingMenu.OUTPUT);
        if (output.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return null;
        }
        ItemStack base = menu.get(SmithingMenu.BASE);
        ItemStack addition = menu.get(SmithingMenu.ADDITION);
        if (base.isEmpty() || addition.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_input", GuiHelper.ERROR);
            return null;
        }
        String template = idOrEmpty(menu.get(SmithingMenu.TEMPLATE));
        return RecipeEntry.smithing(nameText(), template, itemId(base), itemId(addition), itemId(output));
    }
}
