package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.menu.MixingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Mixer / compacter editor: up to three item inputs + one input fluid → one item or one output
 * fluid, with a heat requirement (none / heated / superheated). Fluids are set by tapping a bucket.
 */
public class MixingScreen extends AbstractEditorScreen<MixingMenu> {

    private EditBox fluidInMb;
    private EditBox fluidOutMb;
    private Button heatButton;
    private String heat = "";

    public MixingScreen(MixingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected Station backStation() {
        return menu.station();
    }

    @Override
    protected boolean hasCountBox() {
        return false;
    }

    @Override
    protected int arrowX() {
        return 106;   // midway between the inputs (end 70) and the output column (start 142)
    }

    @Override
    protected void initExtra() {
        fluidInMb = addRenderableWidget(new EditBox(font, leftPos + 40, topPos + 62, 40, 14,
                Component.translatable("gui.easyrecipes.create.mb")));
        fluidInMb.setValue("1000");
        fluidInMb.setMaxLength(6);
        fluidOutMb = addRenderableWidget(new EditBox(font, leftPos + rightEdge() - 40, topPos + 62, 40, 14,
                Component.translatable("gui.easyrecipes.create.mb")));
        fluidOutMb.setValue("1000");
        fluidOutMb.setMaxLength(6);
        heatButton = addRenderableWidget(Button.builder(heatMsg(), b -> {
            heat = "".equals(heat) ? "heated" : "heated".equals(heat) ? "superheated" : "";
            heatButton.setMessage(heatMsg());
        }).bounds(leftPos + 16, topPos + 82, 120, 18).build());
    }

    private Component heatMsg() {
        String key = "heated".equals(heat) ? "heat_heated"
                : "superheated".equals(heat) ? "heat_superheated" : "heat_none";
        return Component.translatable("gui.easyrecipes.create." + key);
    }

    @Override
    protected void renderExtras(GuiGraphics g) {
        // Captions sit 11px above their slot: slot rows are at y=30 and y=60.
        g.drawString(font, Component.translatable("gui.easyrecipes.create.inputs"), leftPos + 16, topPos + 19, GuiHelper.TITLE, false);
        g.drawString(font, Component.translatable("gui.easyrecipes.create.fluid"), leftPos + 16, topPos + 49, GuiHelper.TITLE, false);
        g.drawString(font, Component.translatable("gui.easyrecipes.create.out_item"), leftPos + 142, topPos + 19, GuiHelper.TITLE, false);
        g.drawString(font, Component.translatable("gui.easyrecipes.create.fluid"), leftPos + 142, topPos + 49, GuiHelper.TITLE, false);
    }

    @Override
    protected RecipeEntry buildEntry() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ItemStack s = menu.itemInput(i);
            if (!s.isEmpty()) {
                items.add(itemId(s));
            }
        }
        String fluidIn = Fluids.fluidId(menu.fluidIn());
        ItemStack outItem = menu.outputItem();
        String fluidOut = Fluids.fluidId(menu.fluidOut());

        if (items.isEmpty() && fluidIn.isBlank()) {
            setStatus("gui.easyrecipes.crafting.no_input", GuiHelper.ERROR);
            return null;
        }
        if (outItem.isEmpty() && fluidOut.isBlank()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return null;
        }

        RecipeEntry e = new RecipeEntry();
        e.station = menu.station().name();
        e.name = nameText();
        e.itemInputs = items;
        e.fluidInput = fluidIn;
        e.fluidInputMb = Math.max(1, parseInt(fluidInMb.getValue(), 1000));
        if (!outItem.isEmpty()) {
            e.output = itemId(outItem);
            e.count = outItem.getCount();
        }
        e.fluidOutput = fluidOut;
        e.fluidOutputMb = Math.max(1, parseInt(fluidOutMb.getValue(), 1000));
        e.heat = heat;
        return e;
    }
}
