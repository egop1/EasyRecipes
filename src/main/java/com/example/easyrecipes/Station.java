package com.example.easyrecipes;

import net.minecraftforge.fml.ModList;

/**
 * The stations shown in the picker. {@code requiresCreate} ones are always listed, but are only
 * usable when the Create mod is installed — seeing them tells you what installing Create would add.
 *
 * <p>The brewing stand is deliberately absent. Vanilla brewing is hardcoded in {@code PotionBrewing}
 * instead of being data-driven, and KubeJS has no server-script API for it, so the generator would
 * have nothing to emit — the editor would open and Save would go nowhere. Supporting it would mean
 * Forge's {@code BrewingRecipeRegistry} and a different architecture (registering recipes at runtime
 * and owning our own storage) rather than generating a script.
 */
public enum Station {
    CRAFTING("crafting_table", false, "minecraft:crafting"),
    FURNACE("furnace", false, "minecraft:smelting"),
    BLAST_FURNACE("blast_furnace", false, "minecraft:blasting"),
    SMOKER("smoker", false, "minecraft:smoking"),
    CAMPFIRE("campfire", false, "minecraft:campfire_cooking"),
    STONECUTTER("stonecutter", false, "minecraft:stonecutting"),
    SMITHING("smithing_table", false, "minecraft:smithing"),
    CRUSHING("crushing_wheels", true, "create:crushing"),
    MILLING("millstone", true, "create:milling"),
    PRESSING("mechanical_press", true, "create:pressing"),
    CUTTING("mechanical_saw", true, "create:cutting"),
    FAN_SMELTING("fan_smelting", true, "minecraft:smelting"),
    FAN_BLASTING("fan_blasting", true, "minecraft:blasting"),
    SPLASHING("splashing", true, "create:splashing"),
    HAUNTING("haunting", true, "create:haunting"),
    SANDPAPER("sandpaper", true, "create:sandpaper_polishing"),
    DEPLOYING("deployer", true, "create:deploying"),
    FILLING("spout", true, "create:filling"),
    EMPTYING("item_drain", true, "create:emptying"),
    MIXING("mechanical_mixer", true, "create:mixing"),
    COMPACTING("compacting", true, "create:compacting"),
    MECHANICAL_CRAFTING("mechanical_crafter", true, "create:mechanical_crafting");

    public final String key;
    public final boolean requiresCreate;
    private final String recipeTypeId;

    Station(String key, boolean requiresCreate, String recipeTypeId) {
        this.key = key;
        this.requiresCreate = requiresCreate;
        this.recipeTypeId = recipeTypeId;
    }

    public String translationKey() {
        return "gui.easyrecipes.station." + key;
    }

    public Category category() {
        return requiresCreate ? Category.CREATE : Category.VANILLA;
    }

    /**
     * True for the Create machines whose editor offers a list of weighted results instead of a
     * single one. Purely an interface decision — the script generator emits an outputs array for
     * every Create machine either way.
     *
     * <p>Lives here because both the screen and the menu need the same answer: the menu places the
     * slots and drops the inventory, the screen lays out the list. They used to each carry their
     * own copy of this condition, and haunting was only ever added to neither.
     */
    public boolean weightedOutputs() {
        return this == CRUSHING || this == MILLING || this == SPLASHING || this == HAUNTING;
    }

    /** The vanilla/Create recipe-type id (for editing existing recipes), or null. */
    public String recipeTypeId() {
        return recipeTypeId;
    }

    /**
     * True when this station's required mods are loaded. Every listed station has an editor.
     *
     * <p>Create machines need <em>kubejs_create</em> as well as Create itself: the generated script
     * calls {@code event.recipes.createCrushing(...)}, which that addon adds — Create alone does not
     * teach KubeJS anything. Without it the call is undefined, and one undefined call aborts the
     * whole generated file, taking every other recipe (vanilla ones included) down with it.
     */
    public boolean available() {
        if (requiresCreate) {
            try {
                return ModList.get().isLoaded("create") && ModList.get().isLoaded("kubejs_create");
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static Station byId(int id) {
        Station[] values = values();
        return id >= 0 && id < values.length ? values[id] : CRAFTING;
    }
}
