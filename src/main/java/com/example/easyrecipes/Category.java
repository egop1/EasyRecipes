package com.example.easyrecipes;

/** Top-level grouping shown on the first screen: vanilla stations vs Create stations. */
public enum Category {
    VANILLA("vanilla"),
    CREATE("create");

    public final String key;

    Category(String key) {
        this.key = key;
    }

    public String translationKey() {
        return "gui.easyrecipes.category." + key;
    }
}
