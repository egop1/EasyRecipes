package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.data.RecipeEntry.CreateOutput;
import com.example.easyrecipes.menu.CreateProcessingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Create machine editor. Crushing/milling ("weighted") use one staging output slot + a chance %
 * field + [+]/[-] buttons to build a list of weighted outputs. Pressing/cutting ("simple") use a
 * single input → single output (count from the dragged stack, always 100%).
 */
public class CreateProcessingScreen extends AbstractEditorScreen<CreateProcessingMenu> {

    /** Weighted layout bands (relative to the panel). The list needs a strip of its own: at the
     *  default height it collided with Save, which is drawn after it and painted it over. */
    private static final int LIST_X = 16;
    private static final int LIST_Y = 86;
    /** Cell pitch. The "100%" caption (~24px) is wider than the 16px icon and sets the floor;
     *  30 leaves ~7px between captions and fits one more entry than the old 40 did. */
    private static final int LIST_STEP = 30;
    private static final int LIST_MAX = 6;      // last caption ends at 191, inside the 204 gutter

    private final boolean weighted;
    private EditBox percentBox;
    private EditBox durationBox;
    private final List<CreateOutput> added = new ArrayList<>();

    public CreateProcessingScreen(CreateProcessingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.weighted = menu.station().weightedOutputs();
        if (weighted) {
            // 34px taller than the default: the outputs strip has to fit between the chance field
            // and the controls. CreateProcessingMenu drops the inventory to y=160 to match.
            this.imageHeight = 252;
        }
    }

    @Override
    protected int nameBoxY() {
        return weighted ? 120 : super.nameBoxY();
    }

    @Override
    protected int saveY() {
        return weighted ? 118 : super.saveY();
    }

    @Override
    protected int cancelY() {
        return weighted ? 138 : super.cancelY();
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
        return weighted ? 40 : 128;
    }

    @Override
    protected void initExtra() {
        if (weighted) {
            percentBox = addRenderableWidget(new EditBox(font, leftPos + 50, topPos + 52, 44, 16,
                    Component.translatable("gui.easyrecipes.create.chance")));
            percentBox.setValue("100");
            percentBox.setMaxLength(3);
            addRenderableWidget(Button.builder(Component.literal("+"), b -> onAddOutput())
                    .bounds(leftPos + 100, topPos + 30, 22, 20).build());
            addRenderableWidget(Button.builder(Component.literal("-"), b -> onRemoveOutput())
                    .bounds(leftPos + 124, topPos + 30, 22, 20).build());
            durationBox = addRenderableWidget(new EditBox(font, leftPos + rightEdge() - 50, topPos + 34, 50, 16,
                    Component.translatable("gui.easyrecipes.create.duration")));
        } else {
            // Field + "%" sign together end on the right gutter, like Save/Cancel below them.
            percentBox = addRenderableWidget(new EditBox(font, leftPos + rightEdge() - 50, topPos + 56, 40, 16,
                    Component.translatable("gui.easyrecipes.create.chance")));
            percentBox.setValue("100");
            percentBox.setMaxLength(3);
            durationBox = addRenderableWidget(new EditBox(font, leftPos + 16, topPos + 82, 44, 14,
                    Component.translatable("gui.easyrecipes.create.duration")));
        }
        durationBox.setValue("100");
        durationBox.setMaxLength(6);
    }

    private void onAddOutput() {
        ItemStack out = menu.getOutput();
        if (out.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return;
        }
        int chance = clamp(parseInt(percentBox.getValue(), 100), 1, 100);
        added.add(new CreateOutput(itemId(out), out.getCount(), chance));
        status = Component.empty();
    }

    private void onRemoveOutput() {
        if (!added.isEmpty()) {
            added.remove(added.size() - 1);
        }
    }

    @Override
    protected void renderExtras(GuiGraphics g) {
        if (weighted) {
            g.drawString(font, "%", leftPos + 96, topPos + 56, GuiHelper.TITLE, false);
            g.drawString(font, Component.translatable("gui.easyrecipes.create.duration"), leftPos + rightEdge() - 50, topPos + 24, GuiHelper.TITLE, false);

            // Past LIST_MAX the extras just stop being drawn, so say how many are really there --
            // otherwise pressing [+] looks like it silently did nothing.
            Component outputs = Component.translatable("gui.easyrecipes.create.outputs");
            if (added.size() > LIST_MAX) {
                outputs = outputs.copy().append(Component.literal(" (" + added.size() + ")"));
            }
            g.drawString(font, outputs, leftPos + LIST_X, topPos + 74, GuiHelper.TITLE, false);

            int x = leftPos + LIST_X;
            int y = topPos + LIST_Y;
            for (int i = 0; i < added.size() && i < LIST_MAX; i++) {
                // Newest first: a fresh [+] always lands on the left where you can see it, and the
                // entry pushed off the end is the oldest. Adding used to look like nothing happened.
                CreateOutput o = added.get(added.size() - 1 - i);
                ItemStack stack = GuiHelper.stackOf(o.id, o.count);
                g.renderItem(stack, x, y);
                g.renderItemDecorations(font, stack, x, y);
                g.drawString(font, o.chance + "%", x + 1, y + 18, 0x606060, false);
                x += LIST_STEP;
            }
        } else {
            g.drawString(font, "%", leftPos + rightEdge() - 6, topPos + 60, GuiHelper.TITLE, false);
            g.drawString(font, Component.translatable("gui.easyrecipes.create.duration"), leftPos + 16, topPos + 72, GuiHelper.TITLE, false);
        }
    }

    @Override
    protected RecipeEntry buildEntry() {
        ItemStack input = menu.getInput();
        if (input.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_input", GuiHelper.ERROR);
            return null;
        }

        List<CreateOutput> outputs = new ArrayList<>();
        if (weighted) {
            outputs.addAll(added);
            if (outputs.isEmpty()) {
                ItemStack staging = menu.getOutput();
                if (!staging.isEmpty()) {
                    outputs.add(new CreateOutput(itemId(staging), staging.getCount(),
                            clamp(parseInt(percentBox.getValue(), 100), 1, 100)));
                }
            }
        } else {
            ItemStack out = menu.getOutput();
            if (!out.isEmpty()) {
                int chance = clamp(parseInt(percentBox.getValue(), 100), 1, 100);
                outputs.add(new CreateOutput(itemId(out), out.getCount(), chance));
            }
        }

        if (outputs.isEmpty()) {
            setStatus("gui.easyrecipes.crafting.no_output", GuiHelper.ERROR);
            return null;
        }

        int duration = Math.max(1, parseInt(durationBox.getValue(), 100));
        return RecipeEntry.createProcessing(menu.station().name(), nameText(), itemId(input), outputs, duration);
    }
}
