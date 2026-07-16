package com.example.easyrecipes.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** Small drawing helpers that mimic vanilla's beveled panels and sunken slots (no textures). */
public final class GuiHelper {

    public static final int BORDER = 0xFF000000;
    public static final int FILL = 0xFFC6C6C6;
    public static final int LIGHT = 0xFFFFFFFF;
    public static final int SHADOW = 0xFF555555;
    public static final int TITLE = 0x404040;
    public static final int SLOT_FILL = 0xFF8B8B8B;
    public static final int SLOT_SHADOW = 0xFF373737;
    public static final int ERROR = 0xB00020;
    public static final int OK = 0x18711A;

    private GuiHelper() {}

    /** A vanilla-style raised panel: black outline, light top-left bevel, dark bottom-right bevel. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, FILL);
        g.fill(x + 1, y + 1, x + w - 2, y + 3, LIGHT);
        g.fill(x + 1, y + 1, x + 3, y + h - 2, LIGHT);
        g.fill(x + 3, y + h - 3, x + w - 1, y + h - 1, SHADOW);
        g.fill(x + w - 3, y + 3, x + w - 1, y + h - 1, SHADOW);
    }

    /** A shallow inset strip (used for list rows / grouping). */
    public static void inset(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, SLOT_SHADOW);
        g.fill(x + 1, y + 1, x + w, y + h, LIGHT);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFB0B0B0);
    }

    /** Human-readable form of a stored id (e.g. a TACZ gun shows as {@code tacz:gun/ak47}). */
    public static String displayId(String id) {
        return com.example.easyrecipes.script.CraftingScriptGenerator.display(id);
    }

    /** Resolves an item id to a stack of the given count, or EMPTY if the item is unknown. */
    public static ItemStack stackOf(String id, int count) {
        if (id == null || com.example.easyrecipes.script.CraftingScriptGenerator.isSpecial(id)) {
            return ItemStack.EMPTY; // NBT-identified item (e.g. a TACZ gun) — no plain icon for it.
        }
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

    /** An 18×18 sunken slot at (x,y); the 16×16 item renders at (x+1, y+1). */
    public static void slot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, SLOT_SHADOW);
        g.fill(x + 1, y + 1, x + 18, y + 18, LIGHT);
        g.fill(x + 1, y + 1, x + 17, y + 17, SLOT_FILL);
    }
}
