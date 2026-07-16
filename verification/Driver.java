package com.example.easyrecipes.script;

import com.example.easyrecipes.data.RecipeEntry;

/** Standalone verifier (pure Java, no Minecraft). See README.md for how to run. */
public class Driver {
    static int pass = 0, fail = 0;

    static void check(String label, String actual, String expected) {
        if (actual.equals(expected)) { pass++; System.out.println("PASS " + label); }
        else { fail++; System.out.println("FAIL " + label + "\n--- expected ---\n" + expected + "\n--- actual ---\n" + actual + "\n"); }
    }

    static void checkThrows(String label, Runnable r) {
        try {
            r.run();
            fail++;
            System.out.println("FAIL " + label + " (expected a rejection, got none)");
        } catch (IllegalArgumentException e) {
            pass++;
            System.out.println("PASS " + label);
        }
    }

    static String[] grid(String... c) {
        String[] g = new String[9];
        for (int i = 0; i < 9; i++) g[i] = i < c.length ? c[i] : "";
        return g;
    }

    public static void main(String[] args) {
        String P = "minecraft:oak_planks";

        // --- crafting (direct + via dispatcher) ---
        check("shaped chest", CraftingScriptGenerator.generate("my chest", grid(P, P, P, P, "", P, P, P, P), "minecraft:chest", 1, false),
                "// easyrecipes: my chest\nServerEvents.recipes(event => {\n  event.shaped('minecraft:chest', [\n    'AAA',\n    'A A',\n    'AAA'\n  ], {\n    A: 'minecraft:oak_planks'\n  })\n})\n");
        check("shapeless sticks", CraftingScriptGenerator.generate("sticks", grid(P, P), "minecraft:stick", 4, true),
                "// easyrecipes: sticks\nServerEvents.recipes(event => {\n  event.shapeless('4x minecraft:stick', ['minecraft:oak_planks', 'minecraft:oak_planks'])\n})\n");
        check("dispatch crafting", RecipeScriptGenerator.generate(RecipeEntry.crafting("c", grid("minecraft:diamond"), "minecraft:diamond_block", 1, false)),
                "// easyrecipes: c\nServerEvents.recipes(event => {\n  event.shaped('minecraft:diamond_block', [\n    'A'\n  ], {\n    A: 'minecraft:diamond'\n  })\n})\n");

        // --- cooking ---
        check("smelting xp+time", RecipeScriptGenerator.generate(RecipeEntry.cooking("FURNACE", "iron", "minecraft:raw_iron", "minecraft:iron_ingot", 1, 0.7f, 200)),
                "// easyrecipes: iron\nServerEvents.recipes(event => {\n  event.smelting('minecraft:iron_ingot', 'minecraft:raw_iron').xp(0.7).cookingTime(200)\n})\n");
        check("blasting time only", RecipeScriptGenerator.generate(RecipeEntry.cooking("BLAST_FURNACE", "iron", "minecraft:raw_iron", "minecraft:iron_ingot", 1, 0f, 100)),
                "// easyrecipes: iron\nServerEvents.recipes(event => {\n  event.blasting('minecraft:iron_ingot', 'minecraft:raw_iron').cookingTime(100)\n})\n");
        check("smoking bare", RecipeScriptGenerator.generate(RecipeEntry.cooking("SMOKER", "beef", "minecraft:beef", "minecraft:cooked_beef", 1, 0f, 0)),
                "// easyrecipes: beef\nServerEvents.recipes(event => {\n  event.smoking('minecraft:cooked_beef', 'minecraft:beef')\n})\n");
        check("campfire method name", RecipeScriptGenerator.generate(RecipeEntry.cooking("CAMPFIRE", "beef", "minecraft:beef", "minecraft:cooked_beef", 1, 0.35f, 0)),
                "// easyrecipes: beef\nServerEvents.recipes(event => {\n  event.campfireCooking('minecraft:cooked_beef', 'minecraft:beef').xp(0.35)\n})\n");

        // --- stonecutting ---
        check("stonecutting count", RecipeScriptGenerator.generate(RecipeEntry.stonecutting("bricks", "minecraft:stone", "minecraft:stone_bricks", 4)),
                "// easyrecipes: bricks\nServerEvents.recipes(event => {\n  event.stonecutting('4x minecraft:stone_bricks', 'minecraft:stone')\n})\n");

        // --- smithing (with + without template) ---
        check("smithing 4-arg", RecipeScriptGenerator.generate(RecipeEntry.smithing("ns", "minecraft:netherite_upgrade_smithing_template", "minecraft:diamond_sword", "minecraft:netherite_ingot", "minecraft:netherite_sword")),
                "// easyrecipes: ns\nServerEvents.recipes(event => {\n  event.smithing('minecraft:netherite_sword', 'minecraft:netherite_upgrade_smithing_template', 'minecraft:diamond_sword', 'minecraft:netherite_ingot')\n})\n");
        check("smithing 3-arg", RecipeScriptGenerator.generate(RecipeEntry.smithing("ns", "", "minecraft:diamond_sword", "minecraft:netherite_ingot", "minecraft:netherite_sword")),
                "// easyrecipes: ns\nServerEvents.recipes(event => {\n  event.smithing('minecraft:netherite_sword', 'minecraft:diamond_sword', 'minecraft:netherite_ingot')\n})\n");

        // --- edit override (remove existing id, then re-add) ---
        RecipeEntry edited = RecipeEntry.crafting("myc", grid(P, P, P, P, "", P, P, P, P), "minecraft:chest", 1, false);
        edited.editing = "minecraft:chest";
        check("edit override", RecipeScriptGenerator.generate(edited),
                "// easyrecipes: myc  (edits minecraft:chest)\nServerEvents.recipes(event => {\n  event.remove({ id: 'minecraft:chest' })\n  event.shaped('minecraft:chest', [\n    'AAA',\n    'A A',\n    'AAA'\n  ], {\n    A: 'minecraft:oak_planks'\n  })\n})\n");

        // --- Create processing (crushing / milling / pressing / cutting) ---
        java.util.List<RecipeEntry.CreateOutput> crOut = java.util.List.of(
                new RecipeEntry.CreateOutput("minecraft:gravel", 1, 100),
                new RecipeEntry.CreateOutput("minecraft:flint", 1, 25));
        check("create crushing chance", RecipeScriptGenerator.generate(RecipeEntry.createProcessing("CRUSHING", "crush", "minecraft:cobblestone", crOut, 250)),
                "// easyrecipes: crush\nServerEvents.recipes(event => {\n  event.recipes.createCrushing(['minecraft:gravel', Item.of('minecraft:flint').withChance(0.25)], 'minecraft:cobblestone', 250)\n})\n");

        java.util.List<RecipeEntry.CreateOutput> milOut = java.util.List.of(
                new RecipeEntry.CreateOutput("minecraft:sand", 2, 100),
                new RecipeEntry.CreateOutput("create:experience_nugget", 3, 50));
        check("create milling count+chance", RecipeScriptGenerator.generate(RecipeEntry.createProcessing("MILLING", "m", "minecraft:sandstone", milOut, 100)),
                "// easyrecipes: m\nServerEvents.recipes(event => {\n  event.recipes.createMilling(['2x minecraft:sand', Item.of('create:experience_nugget', 3).withChance(0.5)], 'minecraft:sandstone', 100)\n})\n");

        check("create pressing single", RecipeScriptGenerator.generate(RecipeEntry.createProcessing("PRESSING", "sheet", "minecraft:iron_ingot",
                        java.util.List.of(new RecipeEntry.CreateOutput("create:iron_sheet", 1, 100)), 100)),
                "// easyrecipes: sheet\nServerEvents.recipes(event => {\n  event.recipes.createPressing(['create:iron_sheet'], 'minecraft:iron_ingot', 100)\n})\n");

        // --- more Create machines ---
        check("splashing weighted", RecipeScriptGenerator.generate(RecipeEntry.createProcessing("SPLASHING", "s", "minecraft:sand",
                        java.util.List.of(new RecipeEntry.CreateOutput("minecraft:clay_ball", 1, 50)), 100)),
                "// easyrecipes: s\nServerEvents.recipes(event => {\n  event.recipes.createSplashing([Item.of('minecraft:clay_ball').withChance(0.5)], 'minecraft:sand', 100)\n})\n");

        RecipeEntry dep = new RecipeEntry();
        dep.station = "DEPLOYING"; dep.name = "d"; dep.input = "create:incomplete_precision_mechanism"; dep.heldItem = "create:cogwheel";
        dep.createOutputs = java.util.List.of(new RecipeEntry.CreateOutput("create:incomplete_precision_mechanism", 1, 100));
        dep.keepHeldItem = true;
        check("deploying keep", RecipeScriptGenerator.generate(dep),
                "// easyrecipes: d\nServerEvents.recipes(event => {\n  event.recipes.createDeploying(['create:incomplete_precision_mechanism'], ['create:incomplete_precision_mechanism', 'create:cogwheel']).keepHeldItem()\n})\n");

        RecipeEntry fil = new RecipeEntry();
        fil.station = "FILLING"; fil.name = "f"; fil.input = "minecraft:sponge"; fil.output = "minecraft:wet_sponge"; fil.count = 1; fil.fluidInput = "minecraft:water"; fil.fluidInputMb = 1000;
        check("filling", RecipeScriptGenerator.generate(fil),
                "// easyrecipes: f\nServerEvents.recipes(event => {\n  event.recipes.createFilling('minecraft:wet_sponge', ['minecraft:sponge', Fluid.of('minecraft:water', 1000)])\n})\n");

        RecipeEntry emp = new RecipeEntry();
        emp.station = "EMPTYING"; emp.name = "e"; emp.input = "minecraft:water_bucket"; emp.output = "minecraft:bucket"; emp.count = 1; emp.fluidOutput = "minecraft:water"; emp.fluidOutputMb = 1000;
        check("emptying", RecipeScriptGenerator.generate(emp),
                "// easyrecipes: e\nServerEvents.recipes(event => {\n  event.recipes.createEmptying(['minecraft:bucket', Fluid.of('minecraft:water', 1000)], 'minecraft:water_bucket')\n})\n");

        RecipeEntry mix = new RecipeEntry();
        mix.station = "MIXING"; mix.name = "m"; mix.itemInputs = java.util.List.of("minecraft:sugar", "minecraft:wheat"); mix.fluidInput = "minecraft:water"; mix.fluidInputMb = 250; mix.output = "create:dough"; mix.count = 1; mix.heat = "heated";
        check("mixing heated", RecipeScriptGenerator.generate(mix),
                "// easyrecipes: m\nServerEvents.recipes(event => {\n  event.recipes.createMixing(['create:dough'], ['minecraft:sugar', 'minecraft:wheat', Fluid.of('minecraft:water', 250)]).heated()\n})\n");

        // --- mechanical crafting (any size) ---
        String A = "create:andesite_alloy";
        RecipeEntry mech = new RecipeEntry();
        mech.station = "MECHANICAL_CRAFTING"; mech.name = "lc"; mech.gridW = 3; mech.gridH = 3;
        mech.grid = new String[]{A, "", A, A, A, A, A, "", A};
        mech.output = "create:large_cogwheel"; mech.count = 1;
        check("mechanical 3x3", RecipeScriptGenerator.generate(mech),
                "// easyrecipes: lc\nServerEvents.recipes(event => {\n  event.recipes.createMechanicalCrafting('create:large_cogwheel', [\n    'A A',\n    'AAA',\n    'A A'\n  ], {\n    A: 'create:andesite_alloy'\n  })\n})\n");

        // 5x2 grid, only the middle columns used -> trims to 3 wide; two distinct items; noMirror.
        RecipeEntry mech2 = new RecipeEntry();
        mech2.station = "MECHANICAL_CRAFTING"; mech2.name = "big"; mech2.gridW = 5; mech2.gridH = 2;
        mech2.grid = new String[]{
                "", "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:iron_ingot", "",
                "", "minecraft:stick", "", "minecraft:stick", ""};
        mech2.output = "minecraft:iron_door"; mech2.count = 2; mech2.noMirror = true;
        check("mechanical 5x2 trim + noMirror", RecipeScriptGenerator.generate(mech2),
                "// easyrecipes: big\nServerEvents.recipes(event => {\n  event.recipes.createMechanicalCrafting('2x minecraft:iron_door', [\n    'AAA',\n    'B B'\n  ], {\n    A: 'minecraft:iron_ingot',\n    B: 'minecraft:stick'\n  }).noMirror()\n})\n");

        // --- TACZ items: one registry item + an NBT id, so output builds it and input matches it ---
        String GUN = CraftingScriptGenerator.TACZ_PREFIX + "gun|tacz:ak47|tacz:modern_kinetic_gun";
        String GUN_IN = "Item.of('tacz:modern_kinetic_gun', '{GunId:\"tacz:ak47\"}').weakNBT()";

        // Crafting a gun -> build it.
        check("tacz as shaped output", CraftingScriptGenerator.generate("gun", grid("minecraft:iron_ingot"), GUN, 1, false),
                "// easyrecipes: gun\nServerEvents.recipes(event => {\n  event.shaped(TimelessItem.of('tacz:gun/ak47'), [\n    'A'\n  ], {\n    A: 'minecraft:iron_ingot'\n  })\n})\n");

        // Crafting FROM a gun -> match any gun of that kind, ignoring ammo/fire mode.
        check("tacz in shaped key map (input)", CraftingScriptGenerator.generate("y", grid(GUN), "minecraft:diamond", 1, false),
                "// easyrecipes: y\nServerEvents.recipes(event => {\n  event.shaped('minecraft:diamond', [\n    'A'\n  ], {\n    A: " + GUN_IN + "\n  })\n})\n");

        check("tacz as shapeless ingredient", CraftingScriptGenerator.generate("x", grid(GUN, "minecraft:stick"), "minecraft:diamond", 1, true),
                "// easyrecipes: x\nServerEvents.recipes(event => {\n  event.shapeless('minecraft:diamond', [" + GUN_IN + ", 'minecraft:stick'])\n})\n");

        check("tacz as cooking input", RecipeScriptGenerator.generate(RecipeEntry.cooking("FURNACE", "melt", GUN, "minecraft:iron_ingot", 1, 0f, 0)),
                "// easyrecipes: melt\nServerEvents.recipes(event => {\n  event.smelting('minecraft:iron_ingot', " + GUN_IN + ")\n})\n");

        check("tacz create output with chance", RecipeScriptGenerator.generate(RecipeEntry.createProcessing("CRUSHING", "c", "minecraft:cobblestone",
                        java.util.List.of(new RecipeEntry.CreateOutput(GUN, 1, 50)), 100)),
                "// easyrecipes: c\nServerEvents.recipes(event => {\n  event.recipes.createCrushing([TimelessItem.of('tacz:gun/ak47').withChance(0.5)], 'minecraft:cobblestone', 100)\n})\n");

        // Gun pack: namespace comes from the gun's own id.
        String PACK_GUN = CraftingScriptGenerator.TACZ_PREFIX + "gun|mypack:ak74|tacz:modern_kinetic_gun";
        check("tacz gun pack output", CraftingScriptGenerator.generate("p", grid("minecraft:iron_ingot"), PACK_GUN, 1, false),
                "// easyrecipes: p\nServerEvents.recipes(event => {\n  event.shaped(TimelessItem.of('mypack:gun/ak74'), [\n    'A'\n  ], {\n    A: 'minecraft:iron_ingot'\n  })\n})\n");

        // Addon with its own gun item: keep the item, or TimelessItem would build the stock one.
        String CUSTOM_GUN = CraftingScriptGenerator.TACZ_PREFIX + "gun|mypack:custom|mymod:my_gun";
        check("tacz custom gun item output", CraftingScriptGenerator.generate("cg", grid("minecraft:iron_ingot"), CUSTOM_GUN, 1, false),
                "// easyrecipes: cg\nServerEvents.recipes(event => {\n  event.shaped(TimelessItem.of('mymod:my_gun', 'mypack:gun/custom'), [\n    'A'\n  ], {\n    A: 'minecraft:iron_ingot'\n  })\n})\n");

        check("tacz custom gun item input", CraftingScriptGenerator.generate("ci", grid(CUSTOM_GUN), "minecraft:diamond", 1, true),
                "// easyrecipes: ci\nServerEvents.recipes(event => {\n  event.shapeless('minecraft:diamond', [Item.of('mymod:my_gun', '{GunId:\"mypack:custom\"}').weakNBT()])\n})\n");

        // Ammo uses its own tag.
        String AMMO = CraftingScriptGenerator.TACZ_PREFIX + "ammo|tacz:12g|tacz:ammo";
        check("tacz ammo input tag", CraftingScriptGenerator.generate("a", grid(AMMO), "minecraft:diamond", 1, true),
                "// easyrecipes: a\nServerEvents.recipes(event => {\n  event.shapeless('minecraft:diamond', [Item.of('tacz:ammo', '{AmmoId:\"tacz:12g\"}').weakNBT()])\n})\n");

        // --- legacy ids from an older build must upgrade, not get quoted into broken JS ---
        check("legacy migrate (from the crash log)", CraftingScriptGenerator.migrateLegacy("@js:TimelessItem.of('tacz:gun/taurus500')"),
                CraftingScriptGenerator.TACZ_PREFIX + "gun|tacz:taurus500|tacz:modern_kinetic_gun");
        check("legacy migrate 2-arg", CraftingScriptGenerator.migrateLegacy("@js:TimelessItem.of('mymod:my_gun', 'mypack:gun/custom')"),
                CraftingScriptGenerator.TACZ_PREFIX + "gun|mypack:custom|mymod:my_gun");
        check("legacy migrate ammo", CraftingScriptGenerator.migrateLegacy("@js:TimelessItem.of('tacz:ammo/12g')"),
                CraftingScriptGenerator.TACZ_PREFIX + "ammo|tacz:12g|tacz:ammo");
        check("legacy passthrough plain id", CraftingScriptGenerator.migrateLegacy("minecraft:stone"), "minecraft:stone");
        check("legacy passthrough new format", CraftingScriptGenerator.migrateLegacy(GUN), GUN);

        // The migrated id must now generate valid JS (this is what broke the whole script).
        check("migrated legacy generates valid js",
                CraftingScriptGenerator.generate("g", grid("minecraft:iron_ingot"),
                        CraftingScriptGenerator.migrateLegacy("@js:TimelessItem.of('tacz:gun/taurus500')"), 1, false),
                "// easyrecipes: g\nServerEvents.recipes(event => {\n  event.shaped(TimelessItem.of('tacz:gun/taurus500'), [\n    'A'\n  ], {\n    A: 'minecraft:iron_ingot'\n  })\n})\n");

        // --- ids land inside quotes in generated JS, so anything odd must be refused ---
        checkThrows("rejects quote injection in output", () ->
                CraftingScriptGenerator.generate("bad", grid("minecraft:stone"), "foo'); evil(); ('", 1, false));
        checkThrows("rejects quote injection in ingredient", () ->
                CraftingScriptGenerator.generate("bad", grid("x'); evil(); ('y"), "minecraft:stone", 1, false));
        checkThrows("rejects id without namespace", () ->
                CraftingScriptGenerator.generate("bad", grid("minecraft:stone"), "notanid", 1, false));
        checkThrows("rejects malformed tacz token", () ->
                CraftingScriptGenerator.generate("bad", grid("minecraft:stone"), CraftingScriptGenerator.TACZ_PREFIX + "gun|oops", 1, false));

        RecipeEntry badDelete = new RecipeEntry();
        badDelete.station = "CRAFTING";
        badDelete.deleteOnly = true;
        badDelete.editing = "a'); evil(); ('b";
        checkThrows("rejects injection in delete id", () -> RecipeScriptGenerator.generate(badDelete));

        System.out.println("\n" + pass + " passed, " + fail + " failed");
        if (fail > 0) System.exit(1);
    }
}
