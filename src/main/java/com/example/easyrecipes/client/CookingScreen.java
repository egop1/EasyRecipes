package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.menu.CookingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Furnace-family editor: one input → one output, with optional XP and cooking time. */
public class CookingScreen extends AbstractEditorScreen<CookingMenu> {

    private EditBox xpBox;
    private EditBox timeBox;

    public CookingScreen(CookingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected Station backStation() {
        return menu.station();
    }

    @Override
    protected void initExtra() {
        xpBox = addRenderableWidget(new EditBox(font, leftPos + 16, topPos + 82, 40, 14,
                Component.translatable("gui.easyrecipes.cooking.xp")));
        xpBox.setValue("0");
        xpBox.setMaxLength(6);

        timeBox = addRenderableWidget(new EditBox(font, leftPos + 66, topPos + 82, 46, 14,
                Component.translatable("gui.easyrecipes.cooking.time")));
        timeBox.setHint(Component.literal("ticks"));
        timeBox.setMaxLength(6);
    }

    @Override
    protected void renderExtras(GuiGraphics g) {
        g.drawString(font, Component.translatable("gui.easyrecipes.cooking.xp"), leftPos + 16, topPos + 72, GuiHelper.TITLE, false);
        g.drawString(font, Component.translatable("gui.easyrecipes.cooking.time"), leftPos + 66, topPos + 72, GuiHelper.TITLE, false);
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
        float xp = Math.max(0f, parseFloat(xpBox.getValue(), 0f));
        int time = Math.max(0, parseInt(timeBox.getValue(), 0));
        return RecipeEntry.cooking(menu.station().name(), nameText(), itemId(input), itemId(output), outputCount(), xp, time);
    }
}
