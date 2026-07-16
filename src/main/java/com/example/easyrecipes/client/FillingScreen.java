package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.menu.FillingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Spout filling editor: container item + a fluid (tap a bucket into the fluid slot) → filled item. */
public class FillingScreen extends AbstractEditorScreen<FillingMenu> {

    private EditBox mbBox;

    public FillingScreen(FillingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected Station backStation() {
        return Station.FILLING;
    }

    @Override
    protected boolean hasCountBox() {
        return false;
    }

    @Override
    protected int arrowX() {
        return 128;
    }

    @Override
    protected void initExtra() {
        mbBox = addRenderableWidget(new EditBox(font, leftPos + 68, topPos + 62, 44, 14,
                Component.translatable("gui.easyrecipes.create.mb")));
        mbBox.setValue("1000");
        mbBox.setMaxLength(6);
    }

    @Override
    protected void renderExtras(GuiGraphics g) {
        // Captions sit 11px above their slot: slot rows are at y=30 and y=60.
        g.drawString(font, Component.translatable("gui.easyrecipes.create.item"), leftPos + 44, topPos + 19, GuiHelper.TITLE, false);
        g.drawString(font, Component.translatable("gui.easyrecipes.create.fluid"), leftPos + 44, topPos + 49, GuiHelper.TITLE, false);
        g.drawString(font, "mB", leftPos + 114, topPos + 64, GuiHelper.TITLE, false);
    }

    @Override
    protected RecipeEntry buildEntry() {
        ItemStack container = menu.container();
        if (container.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_input", GuiHelper.ERROR);
            return null;
        }
        ItemStack out = menu.output();
        if (out.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return null;
        }
        String fluidId = Fluids.fluidId(menu.fluidBucket());
        if (fluidId.isBlank()) {
            setStatus("gui.easyrecipes.create.no_fluid", GuiHelper.ERROR);
            return null;
        }
        RecipeEntry e = new RecipeEntry();
        e.station = "FILLING";
        e.name = nameText();
        e.input = itemId(container);
        e.output = itemId(out);
        e.count = out.getCount();
        e.fluidInput = fluidId;
        e.fluidInputMb = Math.max(1, parseInt(mbBox.getValue(), 1000));
        return e;
    }
}
