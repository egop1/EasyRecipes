package com.example.easyrecipes.client;

import com.example.easyrecipes.data.RecipeEntry;
import com.example.easyrecipes.data.RecipeJson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * The client's view of the server-owned recipe manifest. The client never reads or writes the files
 * itself — it only renders what the server last sent.
 */
public final class ClientRecipeCache {

    private static List<RecipeEntry> entries = new ArrayList<>();

    private ClientRecipeCache() {}

    public static List<RecipeEntry> get() {
        return entries;
    }

    /** On logout: the next server's manifest is a different world, stale rows must not leak into it. */
    public static void clear() {
        entries = new ArrayList<>();
    }

    /** Called from the sync packet; refreshes an open list screen. */
    public static void accept(String json) {
        try {
            entries = RecipeJson.listFromJson(json);
        } catch (Exception e) {
            entries = new ArrayList<>();
        }
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof RecipeListScreen list) {
            list.refreshFromCache();
        }
    }
}
