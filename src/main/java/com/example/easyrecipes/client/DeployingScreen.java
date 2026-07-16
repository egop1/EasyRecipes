package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.data.RecipeEntry.CreateOutput;
import com.example.easyrecipes.menu.DeployingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

/** Deployer editor: base + held item → one output (with chance), plus a "keep held item" toggle. */
public class DeployingScreen extends AbstractEditorScreen<DeployingMenu> {

    private EditBox percentBox;
    private Button keepButton;
    private boolean keep = false;

    public DeployingScreen(DeployingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected Station backStation() {
        return Station.DEPLOYING;
    }

    @Override
    protected boolean hasCountBox() {
        return false;
    }

    @Override
    protected void initExtra() {
        // Field + "%" sign together end on the right gutter, like Save/Cancel below them.
        percentBox = addRenderableWidget(new EditBox(font, leftPos + rightEdge() - 50, topPos + 56, 40, 16,
                Component.translatable("gui.easyrecipes.create.chance")));
        percentBox.setValue("100");
        percentBox.setMaxLength(3);
        keepButton = addRenderableWidget(Button.builder(keepMsg(), b -> {
            keep = !keep;
            keepButton.setMessage(keepMsg());
        }).bounds(leftPos + 16, topPos + 82, 120, 18).build());
    }

    private Component keepMsg() {
        return Component.translatable(keep ? "gui.easyrecipes.create.keep_on" : "gui.easyrecipes.create.keep_off");
    }

    @Override
    protected void renderExtras(GuiGraphics g) {
        g.drawString(font, "B", leftPos + 16, topPos + 21, GuiHelper.TITLE, false);
        g.drawString(font, "H", leftPos + 38, topPos + 21, GuiHelper.TITLE, false);
        g.drawString(font, "%", leftPos + rightEdge() - 6, topPos + 60, GuiHelper.TITLE, false);
    }

    @Override
    protected RecipeEntry buildEntry() {
        ItemStack base = menu.base();
        if (base.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_input", GuiHelper.ERROR);
            return null;
        }
        ItemStack held = menu.held();
        if (held.isEmpty()) {
            setStatus("gui.easyrecipes.create.no_held", GuiHelper.ERROR);
            return null;
        }
        ItemStack out = menu.output();
        if (out.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return null;
        }
        int chance = clamp(parseInt(percentBox.getValue(), 100), 1, 100);
        RecipeEntry e = new RecipeEntry();
        e.station = "DEPLOYING";
        e.name = nameText();
        e.input = itemId(base);
        e.heldItem = itemId(held);
        e.createOutputs = new ArrayList<>();
        e.createOutputs.add(new CreateOutput(itemId(out), out.getCount(), chance));
        e.keepHeldItem = keep;
        return e;
    }
}
