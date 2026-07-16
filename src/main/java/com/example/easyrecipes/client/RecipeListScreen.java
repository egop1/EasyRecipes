package com.example.easyrecipes.client;

import com.example.easyrecipes.Station;
import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.data.RecipeJson;
import com.example.easyrecipes.net.Network;
import com.example.easyrecipes.net.OpenEditorPacket;
import com.example.easyrecipes.net.RemoveRecipePacket;
import com.example.easyrecipes.net.RequestRecipesPacket;
import com.example.easyrecipes.net.SaveRecipePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Recipe list for a station: Add / Edit existing / Remove; Edit mode has Tab-autocomplete of ids. */
public class RecipeListScreen extends Screen {

    private static final int W = 260;
    private static final int H = 200;
    private static final int ROW_H = 22;
    private static final int LIST_TOP = 54;

    private final Station station;
    private int left;
    private int top;

    private final List<RecipeEntry> all = new ArrayList<>();
    private final List<RecipeEntry> view = new ArrayList<>();
    private Component status = Component.empty();

    private int scroll = 0;
    private boolean requested = false;
    private boolean editMode = false;
    private EditBox idBox;
    private List<String> stationRecipeIds;
    private List<String> lowerRecipeIds;
    private String lastQuery = null;
    private List<String> cachedMatches = new ArrayList<>();
    private String completionBase = "";
    private String lastCompletion = "";
    private int tabIndex = 0;

    public RecipeListScreen(Station station) {
        super(Component.translatable("gui.easyrecipes.list.title", Component.translatable(station.translationKey())));
        this.station = station;
    }

    private int maxRows() {
        return (H - LIST_TOP - 12) / ROW_H;
    }

    private int maxScroll() {
        return Math.max(0, view.size() - maxRows());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;

        if (!requested) {
            requested = true;
            Network.CHANNEL.sendToServer(new RequestRecipesPacket());
        }

        all.clear();
        view.clear();
        all.addAll(ClientRecipeCache.get());
        for (RecipeEntry entry : all) {
            if (station.name().equals(entry.station)) {
                view.add(entry);
            }
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.list.add"), b -> onAdd())
                .bounds(left + 12, top + 24, 76, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.list.edit"), b -> toggleEditMode())
                .bounds(left + 100, top + 24, 84, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.list.back"),
                        b -> minecraft.setScreen(new StationSelectScreen(station.category())))
                .bounds(left + W - 12 - 52, top + 24, 52, 20).build());

        if (editMode) {
            ensureRecipeIds();
            // 12..140 field, then Open and Delete, ending on the same 12px gutter as everything else.
            idBox = addRenderableWidget(new EditBox(font, left + 12, top + 50, 128, 18,
                    Component.translatable("gui.easyrecipes.list.edit_hint")));
            idBox.setHint(Component.literal(station.key));
            idBox.setMaxLength(128);
            setInitialFocus(idBox);
            addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.list.open"), b -> onOpenEdit())
                    .bounds(left + 144, top + 50, 50, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.easyrecipes.list.delete"), b -> onDelete())
                    .bounds(left + 198, top + 50, 50, 18).build());
        } else {
            // A removal (or a fresh sync) can shrink the list under us, so re-clamp before use.
            scroll = clamp(scroll, 0, maxScroll());
            int rowY = top + LIST_TOP;
            int limit = Math.min(view.size() - scroll, maxRows());
            for (int i = 0; i < limit; i++) {
                final int idx = scroll + i;   // index into view, not into the visible window
                // Same action either way — drop the entry. For a deletion that means the recipe
                // returns to the game, hence the different caption.
                Component caption = view.get(idx).deleteOnly
                        ? Component.translatable("gui.easyrecipes.list.restore")
                        : Component.translatable("gui.easyrecipes.list.remove");
                // Flush with the row inset (rowY, height ROW_H - 4); offsetting by 1 made it hang over.
                addRenderableWidget(Button.builder(caption, b -> onRemove(idx))
                        .bounds(left + W - 12 - 60, rowY, 60, ROW_H - 4).build());
                rowY += ROW_H;
            }
        }
    }

    private void onAdd() {
        if (station == Station.MECHANICAL_CRAFTING) {
            // Grid size must be picked before the menu is built, so ask first.
            minecraft.setScreen(new GridSizeScreen(station, ""));
            return;
        }
        Network.CHANNEL.sendToServer(new OpenEditorPacket(station.ordinal(), ""));
    }

    private void toggleEditMode() {
        editMode = !editMode;
        status = Component.empty();
        rebuildWidgets();
    }

    private void onOpenEdit() {
        String id = idBox.getValue().trim();
        if (id.isEmpty()) {
            return;
        }
        ensureRecipeIds();
        if (!stationRecipeIds.contains(id)) {
            status = Component.translatable("gui.easyrecipes.list.unknown");
            return;
        }
        if (station == Station.MECHANICAL_CRAFTING) {
            minecraft.setScreen(new GridSizeScreen(station, id));
            return;
        }
        Network.CHANNEL.sendToServer(new OpenEditorPacket(station.ordinal(), id));
    }

    /**
     * Takes the typed recipe out of the game. Stored as a normal entry with {@code deleteOnly}, so it
     * shows up in the list next to the created ones — dropping it there puts the recipe back.
     */
    private void onDelete() {
        String id = idBox.getValue().trim();
        if (id.isEmpty()) {
            return;
        }
        ensureRecipeIds();
        if (!stationRecipeIds.contains(id)) {
            status = Component.translatable("gui.easyrecipes.list.unknown");
            return;
        }

        RecipeEntry entry = new RecipeEntry();
        entry.station = station.name();
        entry.editing = id;
        entry.deleteOnly = true;
        entry.name = id;              // the row caption: what got taken out
        copyResultForIcon(entry, id); // purely cosmetic, so the row shows the removed item

        Network.CHANNEL.sendToServer(new SaveRecipePacket(RecipeJson.toJson(entry)));
        editMode = false;             // drop back to the list so the new row is visible
        status = Component.empty();
        rebuildWidgets();
    }

    /** Reads the removed recipe's result so the list row has an icon; failure just means no icon. */
    private void copyResultForIcon(RecipeEntry entry, String id) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        try {
            minecraft.level.getRecipeManager().byKey(new ResourceLocation(id)).ifPresent(recipe -> {
                ItemStack result = recipe.getResultItem(minecraft.level.registryAccess());
                if (result.isEmpty()) {
                    return;
                }
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(result.getItem());
                if (key != null) {
                    entry.output = key.toString();
                    entry.count = result.getCount();
                }
            });
        } catch (Exception ignored) {
            // A recipe type we cannot resolve a result for — the row just renders without an icon.
        }
    }

    private void onRemove(int viewIdx) {
        if (viewIdx < 0 || viewIdx >= view.size()) {
            return;
        }
        // The server owns the files: it removes, reloads and syncs the list back to us.
        Network.CHANNEL.sendToServer(new RemoveRecipePacket(view.get(viewIdx).id));
    }

    /** Called when the server sends a fresh manifest; re-reads the cache. */
    void refreshFromCache() {
        rebuildWidgets();
    }

    /** Shows a message the server pushed (a refused or failed edit). Survives until the next action. */
    void setServerStatus(Component message) {
        this.status = message;
    }

    // --- autocomplete ---

    private RecipeType<?> recipeType() {
        String id = station.recipeTypeId();
        if (id == null) {
            return null;
        }
        // Look up by id (works for Create types too, without a compile dependency on Create).
        return BuiltInRegistries.RECIPE_TYPE.get(new ResourceLocation(id));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void ensureRecipeIds() {
        if (stationRecipeIds != null) {
            return;
        }
        stationRecipeIds = new ArrayList<>();
        lowerRecipeIds = new ArrayList<>();
        RecipeType<?> type = recipeType();
        if (type == null || minecraft == null || minecraft.level == null) {
            return;
        }
        List<? extends Recipe<?>> recipes = minecraft.level.getRecipeManager().getAllRecipesFor((RecipeType) type);
        for (Recipe<?> recipe : recipes) {
            stationRecipeIds.add(recipe.getId().toString());
        }
        stationRecipeIds.sort(String::compareTo);
        // Lowercased once here so the per-keystroke filter never has to redo it.
        for (String id : stationRecipeIds) {
            lowerRecipeIds.add(id.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Cached because render() asks every frame: a station can hold a few thousand recipe ids, and
     * rescanning + lowercasing all of them 60 times a second is pure garbage.
     */
    private List<String> matches(String text) {
        ensureRecipeIds();
        if (text.equals(lastQuery)) {
            return cachedMatches;
        }
        lastQuery = text;
        String needle = text.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < stationRecipeIds.size(); i++) {
            if (needle.isEmpty() || lowerRecipeIds.get(i).contains(needle)) {
                result.add(stationRecipeIds.get(i));
            }
        }
        cachedMatches = result;
        return result;
    }

    private void autocomplete() {
        String current = idBox.getValue();
        if (!current.equals(lastCompletion)) {
            completionBase = current;
            tabIndex = 0;
        }
        List<String> found = matches(completionBase);
        if (found.isEmpty()) {
            return;
        }
        String pick = found.get(tabIndex % found.size());
        tabIndex++;
        idBox.setValue(pick);
        idBox.moveCursorToEnd();
        lastCompletion = pick;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!editMode && maxScroll() > 0) {
            int old = scroll;
            scroll = clamp(scroll - (int) Math.signum(delta), 0, maxScroll());
            if (scroll != old) {
                rebuildWidgets();   // the Remove buttons are bound to absolute indices
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editMode && idBox != null && idBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                autocomplete();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                onOpenEdit();
                return true;
            }
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                return idBox.keyPressed(keyCode, scanCode, modifiers) || idBox.canConsumeInput();
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        GuiHelper.panel(g, left, top, W, H);
        g.drawCenteredString(font, title, left + W / 2, top + 10, GuiHelper.TITLE);

        if (editMode) {
            renderEditMode(g);
        } else {
            renderList(g);
        }

        super.render(g, mouseX, mouseY, partialTick);

        if (!status.getString().isEmpty()) {
            g.drawCenteredString(font, status, left + W / 2, top + H - 11, GuiHelper.ERROR);
        }
    }

    private void renderEditMode(GuiGraphics g) {
        g.drawString(font, Component.translatable("gui.easyrecipes.list.edit_hint"), left + 12, top + 40, GuiHelper.TITLE, false);
        List<String> found = matches(idBox == null ? "" : idBox.getValue());
        int y = top + 74;
        int shown = 0;
        for (String id : found) {
            if (shown >= 6) {
                g.drawString(font, "+" + (found.size() - shown) + " more…", left + 16, y, 0x808080, false);
                break;
            }
            g.drawString(font, id, left + 16, y, 0x606060, false);
            y += 12;
            shown++;
        }
        if (found.isEmpty()) {
            g.drawString(font, Component.translatable("gui.easyrecipes.list.no_match"), left + 16, y, 0x808080, false);
        }
    }

    private void renderList(GuiGraphics g) {
        if (view.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("gui.easyrecipes.list.empty"),
                    left + W / 2, top + LIST_TOP + 16, 0x808080);
            return;
        }
        int rowY = top + LIST_TOP;
        int limit = Math.min(view.size() - scroll, maxRows());
        for (int i = 0; i < limit; i++) {
            RecipeEntry entry = view.get(scroll + i);
            GuiHelper.inset(g, left + 12, rowY, W - 24, ROW_H - 4);
            ItemStack out = resultStack(entry);
            g.renderItem(out, left + 16, rowY + 1);
            g.renderItemDecorations(font, out, left + 16, rowY + 1);
            // Text lives between the icon and the Remove/Restore button. Recipe ids are long enough
            // to run under that button and off the panel, so everything here is width-bounded.
            int textX = left + 38;
            int textMax = (left + W - 12 - 60) - 4 - textX;

            // A "removed" tag would only repeat what the Restore button already says; deletions get
            // that width for their id instead, which is exactly where it is scarce.
            Component tag = !entry.deleteOnly && entry.editing != null && !entry.editing.isBlank()
                    ? Component.translatable("gui.easyrecipes.list.edit_tag")
                    : null;
            int tagW = tag == null ? 0 : font.width(tag) + 4;

            String label = entry.name == null || entry.name.isBlank()
                    ? GuiHelper.displayId(resultId(entry)) : entry.name;
            label = trimToWidth(label, textMax - tagW);
            g.drawString(font, label, textX, rowY + 5, 0x202020, false);
            if (tag != null) {
                g.drawString(font, tag, textX + font.width(label) + 4, rowY + 5, 0x808080, false);
            }
            rowY += ROW_H;
        }

        // Scrollbar, same shape as the one in StationSelectScreen. It replaces the old
        // "+N more" line: that line said rows existed but gave no way to reach them.
        int maxScroll = maxScroll();
        if (maxScroll > 0) {
            int trackX = left + W - 7;
            int trackY = top + LIST_TOP;
            int trackH = maxRows() * ROW_H;
            g.fill(trackX, trackY, trackX + 4, trackY + trackH, 0xFF555555);
            int thumbH = Math.max(12, trackH * maxRows() / view.size());
            int thumbY = trackY + (trackH - thumbH) * scroll / maxScroll;
            g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    /** Cuts text down to {@code maxWidth} pixels, marking with an ellipsis when it had to. */
    private String trimToWidth(String text, int maxWidth) {
        if (maxWidth <= 0 || font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    /** Result item id of any entry. Create machines leave {@code output} empty and put their
     *  results in {@code createOutputs}, so reading {@code output} alone showed no icon at all
     *  for crushing/milling/pressing/cutting/deploying. Fluid-only results have no item. */
    private static String resultId(RecipeEntry e) {
        if (e.output != null && !e.output.isBlank()) {
            return e.output;
        }
        if (e.createOutputs != null && !e.createOutputs.isEmpty()) {
            return e.createOutputs.get(0).id;
        }
        if (e.fluidOutput != null && !e.fluidOutput.isBlank()) {
            return e.fluidOutput;
        }
        return "";
    }

    private static ItemStack resultStack(RecipeEntry e) {
        if (e.output != null && !e.output.isBlank()) {
            return stackOf(e.output, e.count);
        }
        if (e.createOutputs != null && !e.createOutputs.isEmpty()) {
            RecipeEntry.CreateOutput first = e.createOutputs.get(0);
            return stackOf(first.id, first.count);
        }
        return ItemStack.EMPTY;   // fluid-only result: nothing to draw, the label carries it
    }

    private static ItemStack stackOf(String id, int count) {
        try {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item == null) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = new ItemStack(item);
            stack.setCount(Math.max(1, count));
            return stack;
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void onClose() {
        Network.finishEditing();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
