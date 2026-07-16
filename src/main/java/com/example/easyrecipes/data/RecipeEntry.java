package com.example.easyrecipes.data;

/**
 * A single saved recipe of any station. Kept as one flat, Gson-friendly object with a
 * {@code station} discriminator; only the fields relevant to that station are populated.
 */
public class RecipeEntry {

    /** Stable id used to reference this entry over the network (assigned on first save). */
    public String id = "";

    public String station = "CRAFTING";
    public String name = "";

    /** If set, this entry overrides an existing recipe: that recipe id is removed, then re-added. */
    public String editing = "";

    /**
     * When true this entry adds nothing — it only removes {@code editing} from the game.
     *
     * <p>No undo machinery is needed: the script is regenerated from the whole manifest on every
     * change, so dropping this entry simply regenerates it without the removal and the original
     * recipe comes back.
     */
    public boolean deleteOnly = false;

    // Shared single result (crafting / cooking / stonecutting / smithing).
    public String output = "";
    public int count = 1;

    // Crafting (3x3) and mechanical crafting (gridW x gridH), row-major.
    public String[] grid;
    public boolean shapeless = false;

    // Mechanical crafting only.
    public int gridW = 3;
    public int gridH = 3;
    public boolean noMirror = false;
    public boolean noShrink = false;

    // Cooking (furnace family) + stonecutting single input.
    public String input = "";
    public float xp = 0f;
    public int cookingTime = 0; // 0 = let KubeJS use the vanilla default for the type

    // Smithing.
    public String template = "";
    public String base = "";
    public String addition = "";

    // Create processing machines (crushing / milling / pressing / cutting): input + weighted outputs.
    public java.util.List<CreateOutput> createOutputs;
    public int duration = 100;

    // Extra Create inputs (deploying held item; mixing/compacting item inputs).
    public String heldItem = "";
    public java.util.List<String> itemInputs;
    public boolean keepHeldItem = false;

    // Create fluids (filling/emptying/mixing/compacting). Empty id = no fluid on that side.
    public String fluidInput = "";
    public int fluidInputMb = 1000;
    public String fluidOutput = "";
    public int fluidOutputMb = 1000;

    /** Mixing/compacting heat requirement: "", "heated", or "superheated". */
    public String heat = "";

    /** One weighted Create output: item id, stack count, and a 1..100 chance percent. */
    public static class CreateOutput {
        public String id = "";
        public int count = 1;
        public int chance = 100;

        public CreateOutput() {}

        public CreateOutput(String id, int count, int chance) {
            this.id = id;
            this.count = count;
            this.chance = chance;
        }
    }

    public RecipeEntry() {}

    public static RecipeEntry crafting(String name, String[] grid, String output, int count, boolean shapeless) {
        RecipeEntry e = new RecipeEntry();
        e.station = "CRAFTING";
        e.name = name;
        e.grid = grid;
        e.output = output;
        e.count = count;
        e.shapeless = shapeless;
        return e;
    }

    public static RecipeEntry cooking(String station, String name, String input, String output, int count, float xp, int cookingTime) {
        RecipeEntry e = new RecipeEntry();
        e.station = station;
        e.name = name;
        e.input = input;
        e.output = output;
        e.count = count;
        e.xp = xp;
        e.cookingTime = cookingTime;
        return e;
    }

    public static RecipeEntry stonecutting(String name, String input, String output, int count) {
        RecipeEntry e = new RecipeEntry();
        e.station = "STONECUTTER";
        e.name = name;
        e.input = input;
        e.output = output;
        e.count = count;
        return e;
    }

    public static RecipeEntry smithing(String name, String template, String base, String addition, String output) {
        RecipeEntry e = new RecipeEntry();
        e.station = "SMITHING";
        e.name = name;
        e.template = template;
        e.base = base;
        e.addition = addition;
        e.output = output;
        return e;
    }

    public static RecipeEntry createProcessing(String station, String name, String input,
                                               java.util.List<CreateOutput> outputs, int duration) {
        RecipeEntry e = new RecipeEntry();
        e.station = station;
        e.name = name;
        e.input = input;
        e.createOutputs = outputs;
        e.duration = duration;
        return e;
    }
}
