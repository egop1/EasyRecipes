package com.example.easyrecipes.script;

import com.example.easyrecipes.data.RecipeEntry;

/**
 * Dispatches a {@link RecipeEntry} to the correct KubeJS {@code .js} block, based on its station.
 * Pure Java (no Minecraft types) so it stays unit-verifiable. When {@code entry.editing} is set,
 * the block first removes the targeted recipe id, then adds the new one (an override).
 * Method names/arg order confirmed against the kubejs-2001 recipe schemas.
 */
public final class RecipeScriptGenerator {

    private RecipeScriptGenerator() {}

    public static String generate(RecipeEntry e) {
        // A deletion carries no recipe of its own: emit the removal and skip the station dispatch,
        // whose bodies all demand an input/output this entry does not have.
        if (e.deleteOnly) {
            requireId(e.editing, "recipe id");
            String id = CraftingScriptGenerator.requireValidId(e.editing);
            return "// easyrecipes: removes " + id + "\n"
                    + "ServerEvents.recipes(event => {\n"
                    + "  event.remove({ id: '" + id + "' })\n"
                    + "})\n";
        }

        String body;
        switch (e.station) {
            case "CRAFTING":
                body = CraftingScriptGenerator.body(e.grid, e.output, e.count, e.shapeless);
                break;
            case "FURNACE":
                body = cookingBody("smelting", e);
                break;
            case "BLAST_FURNACE":
                body = cookingBody("blasting", e);
                break;
            case "SMOKER":
                body = cookingBody("smoking", e);
                break;
            case "CAMPFIRE":
                body = cookingBody("campfireCooking", e);
                break;
            case "FAN_SMELTING":
                body = cookingBody("smelting", e);
                break;
            case "FAN_BLASTING":
                body = cookingBody("blasting", e);
                break;
            case "STONECUTTER":
                body = stonecuttingBody(e);
                break;
            case "SMITHING":
                body = smithingBody(e);
                break;
            case "CRUSHING":
                body = createProcessingBody("createCrushing", e);
                break;
            case "MILLING":
                body = createProcessingBody("createMilling", e);
                break;
            case "PRESSING":
                body = createProcessingBody("createPressing", e);
                break;
            case "CUTTING":
                body = createProcessingBody("createCutting", e);
                break;
            case "SPLASHING":
                body = createProcessingBody("createSplashing", e);
                break;
            case "HAUNTING":
                body = createProcessingBody("createHaunting", e);
                break;
            case "SANDPAPER":
                body = createProcessingBody("createSandpaperPolishing", e);
                break;
            case "DEPLOYING":
                body = deployingBody(e);
                break;
            case "FILLING":
                body = fillingBody(e);
                break;
            case "EMPTYING":
                body = emptyingBody(e);
                break;
            case "MIXING":
                body = mixingBody("createMixing", e);
                break;
            case "COMPACTING":
                body = mixingBody("createCompacting", e);
                break;
            case "MECHANICAL_CRAFTING":
                body = mechanicalBody(e);
                break;
            default:
                throw new IllegalArgumentException("unknown station: " + e.station);
        }
        return assemble(e.name, e.editing, body);
    }

    private static String assemble(String name, String editing, String body) {
        boolean edit = editing != null && !editing.isBlank();
        if (edit) {
            CraftingScriptGenerator.requireValidId(editing);
        }
        String header = "// easyrecipes: " + CraftingScriptGenerator.safeComment(name)
                + (edit ? "  (edits " + editing.trim() + ")" : "") + "\n";
        String remove = edit ? "  event.remove({ id: '" + editing.trim() + "' })\n" : "";
        return header + "ServerEvents.recipes(event => {\n" + remove + body + "})\n";
    }

    // event.smelting(result, ingredient).xp(f).cookingTime(t)  — xp/time emitted only when set.
    static String cookingBody(String method, RecipeEntry e) {
        requireId(e.input, "input");
        requireId(e.output, "output");
        StringBuilder call = new StringBuilder();
        call.append("  event.").append(method).append("(")
                .append(CraftingScriptGenerator.outputToken(e.output.trim(), Math.max(1, e.count)))
                .append(", ").append(ref(e.input)).append(")");
        if (e.xp > 0f) {
            call.append(".xp(").append(formatFloat(e.xp)).append(")");
        }
        if (e.cookingTime > 0) {
            call.append(".cookingTime(").append(e.cookingTime).append(")");
        }
        call.append("\n");
        return call.toString();
    }

    // event.stonecutting(result, ingredient)  — result count supported.
    static String stonecuttingBody(RecipeEntry e) {
        requireId(e.input, "input");
        requireId(e.output, "output");
        return "  event.stonecutting("
                + CraftingScriptGenerator.outputToken(e.output.trim(), Math.max(1, e.count))
                + ", " + ref(e.input) + ")\n";
    }

    // event.smithing(result, template, base, addition)  — 3-arg form when no template.
    static String smithingBody(RecipeEntry e) {
        requireId(e.base, "base");
        requireId(e.addition, "addition");
        requireId(e.output, "output");
        String result = CraftingScriptGenerator.outputToken(e.output, Math.max(1, e.count));
        if (e.template == null || e.template.isBlank()) {
            return "  event.smithing(" + result + ", " + ref(e.base) + ", " + ref(e.addition) + ")\n";
        }
        return "  event.smithing(" + result + ", " + ref(e.template) + ", "
                + ref(e.base) + ", " + ref(e.addition) + ")\n";
    }

    // event.recipes.createCrushing([outputs], 'input', duration)  — outputs FIRST, input SECOND.
    static String createProcessingBody(String method, com.example.easyrecipes.data.RecipeEntry e) {
        requireId(e.input, "input");
        if (e.createOutputs == null || e.createOutputs.isEmpty()) {
            throw new IllegalArgumentException("at least one output is required");
        }
        StringBuilder outs = new StringBuilder();
        for (int i = 0; i < e.createOutputs.size(); i++) {
            if (i > 0) {
                outs.append(", ");
            }
            outs.append(createOutputToken(e.createOutputs.get(i)));
        }
        return "  event.recipes." + method + "([" + outs + "], " + ref(e.input) + ", "
                + Math.max(1, e.duration) + ")\n";
    }

    /** {@code 'id'}, {@code '2x id'}, or {@code Item.of('id'[, n]).withChance(0.25)} for chance < 100. */
    static String createOutputToken(RecipeEntry.CreateOutput o) {
        String id = o.id.trim();
        int count = Math.max(1, o.count);
        if (CraftingScriptGenerator.isTacz(id)) {
            String base = CraftingScriptGenerator.outputToken(id, count);
            return o.chance >= 100 ? base : base + ".withChance(" + formatChance(o.chance) + ")";
        }
        if (o.chance >= 100) {
            return count > 1 ? "'" + count + "x " + id + "'" : "'" + id + "'";
        }
        String base = count > 1 ? "Item.of('" + id + "', " + count + ")" : "Item.of('" + id + "')";
        return base + ".withChance(" + formatChance(o.chance) + ")";
    }

    static String formatChance(int percent) {
        return Double.toString(percent / 100.0);
    }

    /** A quoted item id, or a raw JS expression for NBT-identified items (TACZ guns etc.). */
    private static String ref(String id) {
        return CraftingScriptGenerator.ref(id);
    }

    private static String fluid(String id, int mb) {
        return "Fluid.of('" + CraftingScriptGenerator.requireValidId(id) + "', " + Math.max(1, mb) + ")";
    }

    private static String weightedOutputs(RecipeEntry e) {
        if (e.createOutputs == null || e.createOutputs.isEmpty()) {
            throw new IllegalArgumentException("at least one output is required");
        }
        StringBuilder outs = new StringBuilder();
        for (int i = 0; i < e.createOutputs.size(); i++) {
            if (i > 0) {
                outs.append(", ");
            }
            outs.append(createOutputToken(e.createOutputs.get(i)));
        }
        return outs.toString();
    }

    // event.recipes.createDeploying([outputs], ['base', 'held'])[.keepHeldItem()]
    static String deployingBody(RecipeEntry e) {
        requireId(e.input, "base");
        requireId(e.heldItem, "held item");
        String outs = weightedOutputs(e);
        return "  event.recipes.createDeploying([" + outs + "], [" + ref(e.input) + ", "
                + ref(e.heldItem) + "])" + (e.keepHeldItem ? ".keepHeldItem()" : "") + "\n";
    }

    // event.recipes.createFilling(output, ['container', Fluid.of(...)])
    static String fillingBody(RecipeEntry e) {
        requireId(e.output, "output");
        requireId(e.input, "container");
        requireId(e.fluidInput, "input fluid");
        return "  event.recipes.createFilling("
                + CraftingScriptGenerator.outputToken(e.output.trim(), Math.max(1, e.count))
                + ", [" + ref(e.input) + ", " + fluid(e.fluidInput, e.fluidInputMb) + "])\n";
    }

    // event.recipes.createEmptying([outputItem, Fluid.of(...)], 'input')
    static String emptyingBody(RecipeEntry e) {
        requireId(e.output, "output item");
        requireId(e.input, "input");
        requireId(e.fluidOutput, "output fluid");
        return "  event.recipes.createEmptying(["
                + CraftingScriptGenerator.outputToken(e.output.trim(), Math.max(1, e.count))
                + ", " + fluid(e.fluidOutput, e.fluidOutputMb) + "], " + ref(e.input) + ")\n";
    }

    // event.recipes.createMixing([result], [ingredients])[.heated()/.superheated()]
    static String mixingBody(String method, RecipeEntry e) {
        String result;
        if (e.fluidOutput != null && !e.fluidOutput.isBlank()) {
            result = fluid(e.fluidOutput, e.fluidOutputMb);
        } else if (e.output != null && !e.output.isBlank()) {
            result = CraftingScriptGenerator.outputToken(e.output.trim(), Math.max(1, e.count));
        } else {
            throw new IllegalArgumentException("an item or fluid output is required");
        }

        StringBuilder ing = new StringBuilder();
        if (e.itemInputs != null) {
            for (String id : e.itemInputs) {
                if (id != null && !id.isBlank()) {
                    if (ing.length() > 0) {
                        ing.append(", ");
                    }
                    ing.append(ref(id));
                }
            }
        }
        if (e.fluidInput != null && !e.fluidInput.isBlank()) {
            if (ing.length() > 0) {
                ing.append(", ");
            }
            ing.append(fluid(e.fluidInput, e.fluidInputMb));
        }
        if (ing.length() == 0) {
            throw new IllegalArgumentException("at least one input is required");
        }

        String heatSuffix = "heated".equals(e.heat) ? ".heated()"
                : "superheated".equals(e.heat) ? ".superheated()" : "";
        return "  event.recipes." + method + "([" + result + "], [" + ing + "])" + heatSuffix + "\n";
    }

    // event.recipes.createMechanicalCrafting(result, ['rows'], { A: 'id' })[.noMirror()][.noShrink()]
    static String mechanicalBody(RecipeEntry e) {
        requireId(e.output, "output");
        int w = Math.max(1, e.gridW);
        int h = Math.max(1, e.gridH);
        if (e.grid == null || e.grid.length != w * h) {
            throw new IllegalArgumentException("grid must have exactly " + (w * h) + " cells");
        }
        String[] cells = new String[w * h];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = e.grid[i] == null ? "" : e.grid[i].trim();
        }
        CraftingScriptGenerator.Shape shape = CraftingScriptGenerator.shape(cells, w, h);
        String suffix = (e.noMirror ? ".noMirror()" : "") + (e.noShrink ? ".noShrink()" : "");
        return "  event.recipes.createMechanicalCrafting("
                + CraftingScriptGenerator.outputToken(e.output.trim(), Math.max(1, e.count)) + ", [\n"
                + shape.rowsBlock() + "\n"
                + "  ], {\n"
                + shape.keysBlock() + "\n"
                + "  })" + suffix + "\n";
    }

    private static void requireId(String id, String what) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(what + " item is required");
        }
    }

    static String formatFloat(float value) {
        if (value == Math.rint(value)) {
            return (int) value + ".0";
        }
        return Float.toString(value);
    }
}
