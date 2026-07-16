package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.data.RecipeJson;
import com.example.easyrecipes.menu.GhostMenu;
import com.example.easyrecipes.net.Network;
import com.example.easyrecipes.net.SaveRecipePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;


/**
 * Shared base for all station editors. Handles the common controls (name, output count, Save,
 * Cancel), sending the built entry to the server to persist, and returning to the recipe list.
 * It also auto-draws a beveled panel and a sunken background behind every menu slot (shifted 1px
 * up-left so items sit centered). Subclasses only add their layout-specific widgets and build the
 * {@link RecipeEntry}.
 */
public abstract class AbstractEditorScreen<M extends GhostMenu> extends AbstractContainerScreen<M> {

    /** Gutter on both sides. Right-hand widgets are placed off {@link #rightEdge()}, not hardcoded,
     *  so they line up with the left column instead of each ending at its own arbitrary x. */
    protected static final int MARGIN = 16;

    /** A labelled slot needs label (9) + gap (2) + slot (18). Rows closer than this collide. */
    protected static final int LABELLED_ROW = 29;

    protected EditBox nameBox;
    protected EditBox countBox;
    protected Component status = Component.empty();
    protected int statusColor = GuiHelper.TITLE;

    /** x where the right column ends (mirrors the left gutter). */
    protected int rightEdge() {
        return imageWidth - MARGIN;
    }

    protected AbstractEditorScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 220;
        // Tall enough that the control band (ends at y=122) clears the inventory (starts at y=126)
        // and the status line still fits below the hotbar.
        this.imageHeight = 218;
    }

    /** Station to return to (the recipe list) after Save/Cancel. */
    protected abstract Station backStation();

    /** Build the entry to save, or set a status message and return null. */
    protected abstract RecipeEntry buildEntry();

    /** Add station-specific widgets (called at the end of init). */
    protected void initExtra() {}

    /** Draw station-specific decorations; the panel and slot backgrounds are already drawn. */
    protected void renderExtras(GuiGraphics g) {}

    /** Whether to show the output-count box. Off for smithing (single result). */
    protected boolean hasCountBox() {
        return true;
    }

    /** Position of the "→" arrow between inputs and the output area (relative to the panel). */
    protected int arrowX() {
        return 128;
    }

    protected int arrowY() {
        return 40;
    }

    // Control positions (relative to the panel); overridable for layouts of a different size.
    protected int nameBoxX() {
        return 16;
    }

    protected int nameBoxY() {
        return 104;
    }

    protected int saveX() {
        return rightEdge() - 60;
    }

    protected int saveY() {
        return 82;
    }

    protected int cancelX() {
        return rightEdge() - 60;
    }

    protected int cancelY() {
        return 104;
    }

    @Override
    protected void init() {
        super.init();

        // Sits on the output slot's own line (every menu with a count box puts that slot at y=36),
        // vertically centred against it. Below the slot it read as an unrelated stray field.
        if (hasCountBox()) {
            countBox = addRenderableWidget(new EditBox(font, leftPos + rightEdge() - 30, topPos + 38, 30, 14,
                    Component.translatable("gui.easyrecipes.crafting.count")));
            countBox.setValue("1");
            countBox.setMaxLength(3);
        }

        nameBox = addRenderableWidget(new EditBox(font, leftPos + nameBoxX(), topPos + nameBoxY(), 96, 16,
                Component.translatable("gui.easyrecipes.crafting.name")));
        nameBox.setHint(Component.literal("name"));
        nameBox.setMaxLength(64);

        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.crafting.save"), b -> onSave())
                .bounds(leftPos + saveX(), topPos + saveY(), 60, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.crafting.cancel"), b -> backToList())
                .bounds(leftPos + cancelX(), topPos + cancelY(), 60, 18).build());

        initExtra();
    }

    protected int outputCount() {
        return countBox == null ? 1 : Math.max(1, parseInt(countBox.getValue(), 1));
    }

    protected String nameText() {
        return nameBox.getValue();
    }

    private void onSave() {
        RecipeEntry entry = buildEntry();
        if (entry == null) {
            return;
        }
        entry.editing = menu.editingId();
        // The server owns the script files: send the entry and let it write, reload and sync back.
        Network.CHANNEL.sendToServer(new SaveRecipePacket(RecipeJson.toJson(entry)));
        backToList();
    }

    protected void backToList() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
        if (minecraft != null) {
            minecraft.setScreen(new RecipeListScreen(backStation()));
        }
    }

    protected void setStatus(String key, int color) {
        this.status = Component.translatable(key);
        this.statusColor = color;
    }

    protected static String itemId(ItemStack stack) {
        // TACZ-style items share one registry id and keep their identity in NBT — emit their
        // KubeJS builder call instead of a plain id, which would mean "any gun".
        String nbtIdentified = TaczItems.token(stack);
        if (nbtIdentified != null) {
            return nbtIdentified;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "minecraft:air" : id.toString();
    }

    protected static String idOrEmpty(ItemStack stack) {
        return stack.isEmpty() ? "" : itemId(stack);
    }

    protected static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    protected static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    protected static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int bx = leftPos - 1;
        int by = topPos - 1;
        GuiHelper.panel(g, bx, by, imageWidth + 1, imageHeight + 1);
        for (Slot slot : menu.slots) {
            GuiHelper.slot(g, leftPos + slot.x - 1, topPos + slot.y - 1);
        }
        g.drawString(font, "→", leftPos + arrowX(), topPos + arrowY(), GuiHelper.TITLE, false);
        renderExtras(g);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, 6, GuiHelper.TITLE, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        if (!status.getString().isEmpty()) {
            g.drawCenteredString(font, status, leftPos + imageWidth / 2, topPos + imageHeight - 12, statusColor);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (getFocused() instanceof EditBox box && box.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return box.keyPressed(keyCode, scanCode, modifiers) || box.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        // Only fires on Esc — backToList() swaps screens without going through here.
        Network.finishEditing();
        super.onClose();
    }
}
