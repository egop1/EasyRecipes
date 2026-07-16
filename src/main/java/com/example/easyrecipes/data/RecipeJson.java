package com.example.easyrecipes.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gson (de)serialization for recipe entries. Used both for the on-disk manifest and as the network
 * payload — sending the whole entry as JSON avoids hand-writing an encoder for every field.
 */
public final class RecipeJson {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private RecipeJson() {}

    public static String toJson(RecipeEntry entry) {
        return GSON.toJson(entry);
    }

    public static RecipeEntry fromJson(String json) {
        return GSON.fromJson(json, RecipeEntry.class);
    }

    public static String listToJson(List<RecipeEntry> entries) {
        return GSON.toJson(entries);
    }

    public static List<RecipeEntry> listFromJson(String json) {
        RecipeEntry[] entries = GSON.fromJson(json, RecipeEntry[].class);
        return entries == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(entries));
    }
}
