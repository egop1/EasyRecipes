package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.menu.MechanicalCraftingMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Mechanical crafter editor sized to the grid chosen beforehand. Empty cells become spaces in the
 * pattern (no ingredient at that position), and empty border rows/columns are trimmed on save.
 */
public class MechanicalCraftingScreen extends AbstractEditorScreen<MechanicalCraftingMenu> {

    private Button mirrorButton;
    private Button shrinkButton;
    private boolean noMirror = false;
    private boolean noShrink = false;

    public MechanicalCraftingScreen(MechanicalCraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = MechanicalCraftingMenu.panelWidth(menu.gridW());
        this.imageHeight = MechanicalCraftingMenu.panelHeight(menu.gridH());
    }

    private int gridBottom() {
        return MechanicalCraftingMenu.GRID_Y + menu.gridH() * 18;
    }

    @Override
    protected Station backStation() {
        return Station.MECHANICAL_CRAFTING;
    }

    @Override
    protected boolean hasCountBox() {
        return false;
    }

    @Override
    protected int arrowX() {
        return MechanicalCraftingMenu.GRID_X + menu.gridW() * 18 + 5;
    }

    @Override
    protected int arrowY() {
        return MechanicalCraftingMenu.outputY(menu.gridH()) + 4;
    }

    @Override
    protected int nameBoxX() {
        return 8;
    }

    @Override
    protected int nameBoxY() {
        return gridBottom() + 6;
    }

    @Override
    protected int saveX() {
        return 110;
    }

    @Override
    protected int saveY() {
        return gridBottom() + 6;
    }

    @Override
    protected int cancelX() {
        return 174;
    }

    @Override
    protected int cancelY() {
        return gridBottom() + 6;
    }

    @Override
    protected void initExtra() {
        mirrorButton = addRenderableWidget(Button.builder(mirrorMsg(), b -> {
            noMirror = !noMirror;
            mirrorButton.setMessage(mirrorMsg());
        }).bounds(leftPos + 8, topPos + gridBottom() + 28, 100, 18).build());

        shrinkButton = addRenderableWidget(Button.builder(shrinkMsg(), b -> {
            noShrink = !noShrink;
            shrinkButton.setMessage(shrinkMsg());
        }).bounds(leftPos + 112, topPos + gridBottom() + 28, 100, 18).build());
    }

    private Component mirrorMsg() {
        return Component.translatable(noMirror ? "gui.easyrecipes.mech.mirror_off" : "gui.easyrecipes.mech.mirror_on");
    }

    private Component shrinkMsg() {
        return Component.translatable(noShrink ? "gui.easyrecipes.mech.shrink_off" : "gui.easyrecipes.mech.shrink_on");
    }

    @Override
    protected RecipeEntry buildEntry() {
        ItemStack out = menu.output();
        if (out.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return null;
        }

        int w = menu.gridW();
        int h = menu.gridH();
        String[] grid = new String[w * h];
        boolean any = false;
        for (int i = 0; i < grid.length; i++) {
            grid[i] = idOrEmpty(menu.gridItem(i));
            if (!grid[i].isEmpty()) {
                any = true;
            }
        }
        if (!any) {
            setStatus("gui.easyrecipes.crafting.no_input", GuiHelper.ERROR);
            return null;
        }

        RecipeEntry e = new RecipeEntry();
        e.station = "MECHANICAL_CRAFTING";
        e.name = nameText();
        e.gridW = w;
        e.gridH = h;
        e.grid = grid;
        e.output = itemId(out);
        e.count = out.getCount();
        e.noMirror = noMirror;
        e.noShrink = noShrink;
        return e;
    }
}
