package com.example.easyrecipes.script;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Pure, side-effect-free builder of a KubeJS crafting-table recipe block.
 *
 * <p>Emits the {@code ServerEvents.recipes} form confirmed against the kubejs-2001 source:
 * <ul>
 *   <li>shaped: {@code event.shaped(output, [patternRows], { A: 'id', ... })}</li>
 *   <li>shapeless: {@code event.shapeless(output, [ingredients])}</li>
 *   <li>output count &gt; 1: string shorthand {@code '<n>x <id>'}</li>
 * </ul>
 *
 * <p>No Minecraft/Forge types are referenced, so this is unit-verifiable in isolation.
 */
public final class CraftingScriptGenerator {

    public static final int SIZE = 3;
    public static final int CELLS = SIZE * SIZE;

    private CraftingScriptGenerator() {}

    /**
     * @param name        free-text recipe name (used only as a comment)
     * @param grid        row-major 3x3 item ids; null/blank entries are empty cells
     * @param outputId    result item id, e.g. {@code minecraft:chest}
     * @param outputCount result count (>= 1)
     * @param shapeless   true for a shapeless recipe, false for shaped
     */
    public static String generate(String name, String[] grid, String outputId, int outputCount, boolean shapeless) {
        return "// easyrecipes: " + safeComment(name) + "\n"
                + "ServerEvents.recipes(event => {\n"
                + body(grid, outputId, outputCount, shapeless)
                + "})\n";
    }

    /** The inner {@code   event.shaped/shapeless(...)} call line (no comment/wrapper). Throws if invalid. */
    static String body(String[] grid, String outputId, int outputCount, boolean shapeless) {
        if (grid == null || grid.length != CELLS) {
            throw new IllegalArgumentException("grid must have exactly " + CELLS + " cells");
        }
        if (outputId == null || outputId.isBlank()) {
            throw new IllegalArgumentException("output item id is required");
        }

        String[] cells = new String[CELLS];
        boolean any = false;
        for (int i = 0; i < CELLS; i++) {
            String c = grid[i] == null ? "" : grid[i].trim();
            cells[i] = c;
            any |= !c.isEmpty();
        }
        if (!any) {
            throw new IllegalArgumentException("at least one input item is required");
        }

        String output = outputToken(outputId.trim(), Math.max(1, outputCount));
        return shapeless ? shapelessCall(output, cells) : shapedCall(output, cells);
    }

    /**
     * A TACZ-style item: one registry item plus an NBT id. Stored as
     * {@code @tacz:<type>|<namespace>:<path>|<itemId>} rather than as a finished JS expression,
     * because an output and an input need <em>different</em> JS: an output must build the gun,
     * while an input must match any gun of that kind.
     */
    public static final String TACZ_PREFIX = "@tacz:";

    /** True for any stored id that is not a plain item id. */
    public static boolean isSpecial(String id) {
        return id != null && id.startsWith("@");
    }

    /**
     * Ids arrive from the client and end up inside quotes in generated JS. A stray quote would
     * break the whole script — Rhino aborts the file — or run whatever follows it, so anything that
     * is not a plain {@code namespace:path} is rejected outright.
     */
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    static String requireValidId(String id) {
        String s = id == null ? "" : id.trim();
        if (!VALID_ID.matcher(s).matches()) {
            throw new IllegalArgumentException("invalid id: " + s);
        }
        return s;
    }

    static boolean isTacz(String id) {
        return id != null && id.startsWith(TACZ_PREFIX);
    }

    private static String[] taczParts(String id) {
        String[] parts = id.trim().substring(TACZ_PREFIX.length()).split("\\|", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("malformed item token: " + id);
        }
        return parts;
    }

    private static String taczTagKey(String type) {
        return switch (type) {
            case "attachment" -> "AttachmentId";
            case "ammo" -> "AmmoId";
            default -> "GunId";
        };
    }

    private static String taczDefaultItem(String type) {
        return switch (type) {
            case "attachment" -> "tacz:attachment";
            case "ammo" -> "tacz:ammo";
            default -> "tacz:modern_kinetic_gun";
        };
    }

    /** {@code tacz:gun/ak47} — TACZ's own index form. */
    private static String taczIndex(String type, String indexId) {
        int colon = indexId.indexOf(':');
        return indexId.substring(0, colon) + ":" + type + "/" + indexId.substring(colon + 1);
    }

    /** Builds the real item — for OUTPUT positions. */
    private static String taczOut(String[] p) {
        String itemId = requireValidId(p[2]);
        String index = "'" + taczIndex(p[0], requireValidId(p[1])) + "'";
        return itemId.equals(taczDefaultItem(p[0]))
                ? "TimelessItem.of(" + index + ")"
                : "TimelessItem.of('" + itemId + "', " + index + ")";
    }

    /**
     * Matches any item carrying that NBT id — for INPUT positions. {@code TimelessItem.of()} would
     * build a gun with 0 ammo and an UNKNOWN fire mode and then only accept one in exactly that
     * state, so a gun out of a player's inventory would never match. weakNBT() compares only the
     * tags we name.
     */
    private static String taczIn(String[] p) {
        return "Item.of('" + requireValidId(p[2]) + "', '{" + taczTagKey(p[0])
                + ":\"" + requireValidId(p[1]) + "\"}').weakNBT()";
    }

    /**
     * Upgrades ids written by an older build, which stored TACZ items as a finished JS expression
     * ({@code @js:TimelessItem.of('tacz:gun/ak47')}). This build stores them as data instead, and
     * would treat the old marker as an ordinary id — quoting it and breaking the whole script.
     * Returns the id unchanged when there is nothing to migrate.
     */
    public static String migrateLegacy(String id) {
        if (id == null || !id.startsWith("@js:TimelessItem.of(")) {
            return id;
        }
        int open = id.indexOf('(');
        int close = id.lastIndexOf(')');
        if (open < 0 || close <= open) {
            return id;
        }

        List<String> args = new ArrayList<>();
        for (String part : id.substring(open + 1, close).split(",")) {
            String arg = part.trim();
            if (arg.length() >= 2 && arg.startsWith("'") && arg.endsWith("'")) {
                arg = arg.substring(1, arg.length() - 1);
            }
            if (!arg.isEmpty()) {
                args.add(arg);
            }
        }
        if (args.isEmpty()) {
            return id;
        }

        // Last arg is always the index ("tacz:gun/ak47"); a first arg means a custom gun item.
        String index = args.get(args.size() - 1);
        int colon = index.indexOf(':');
        int slash = index.indexOf('/');
        if (colon < 0 || slash < colon) {
            return id;
        }
        String namespace = index.substring(0, colon);
        String type = index.substring(colon + 1, slash);
        String path = index.substring(slash + 1);
        String itemId = args.size() > 1 ? args.get(0) : taczDefaultItem(type);
        return TACZ_PREFIX + type + "|" + namespace + ":" + path + "|" + itemId;
    }

    /** Human-readable form of a stored id, for the GUI. */
    public static String display(String id) {
        return isTacz(id) ? taczIndex(taczParts(id)[0], taczParts(id)[1]) : id;
    }

    /** An INPUT reference: a quoted id, or a matcher for NBT-identified items. */
    static String ref(String id) {
        String s = id == null ? "" : id.trim();
        return isTacz(s) ? taczIn(taczParts(s)) : "'" + requireValidId(s) + "'";
    }

    /** An OUTPUT reference: {@code 'minecraft:chest'}, {@code '4x minecraft:stick'}, or a builder call. */
    static String outputToken(String id, int count) {
        String s = id == null ? "" : id.trim();
        if (isTacz(s)) {
            String base = taczOut(taczParts(s));
            return count > 1 ? base + ".withCount(" + count + ")" : base;
        }
        String valid = requireValidId(s);
        return count > 1 ? "'" + count + "x " + valid + "'" : "'" + valid + "'";
    }

    private static String shapelessCall(String output, String[] cells) {
        StringBuilder ingredients = new StringBuilder();
        boolean first = true;
        for (String cell : cells) {
            if (cell.isEmpty()) {
                continue;
            }
            if (!first) {
                ingredients.append(", ");
            }
            ingredients.append(ref(cell));
            first = false;
        }
        return "  event.shapeless(" + output + ", [" + ingredients + "])\n";
    }

    private static String shapedCall(String output, String[] cells) {
        Shape shape = shape(cells, SIZE, SIZE);
        return "  event.shaped(" + output + ", [\n"
                + shape.rowsBlock() + "\n"
                + "  ], {\n"
                + shape.keysBlock() + "\n"
                + "  })\n";
    }

    /** A trimmed pattern (rows, space = empty cell) plus the letter → item id map. */
    static final class Shape {

        final List<String> rows;
        final Map<String, Character> letters;

        Shape(List<String> rows, Map<String, Character> letters) {
            this.rows = rows;
            this.letters = letters;
        }

        /** Pattern rows as indented, quoted lines. */
        String rowsBlock() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rows.size(); i++) {
                if (i > 0) {
                    sb.append(",\n");
                }
                sb.append("    '").append(rows.get(i)).append("'");
            }
            return sb.toString();
        }

        /** Key map as indented {@code A: 'id'} lines. */
        String keysBlock() {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Character> entry : letters.entrySet()) {
                if (!first) {
                    sb.append(",\n");
                }
                sb.append("    ").append(entry.getValue()).append(": ").append(ref(entry.getKey()));
                first = false;
            }
            return sb.toString();
        }
    }

    /**
     * Trims empty border rows/columns down to the minimal bounding box and assigns a distinct letter
     * (in first-seen order) to each distinct item. Shared by shaped crafting (3×3) and mechanical
     * crafting (any W×H) — they differ only in grid size.
     *
     * @param cells row-major grid of item ids; empty string = empty cell
     */
    static Shape shape(String[] cells, int w, int h) {
        int minRow = h, maxRow = -1, minCol = w, maxCol = -1;
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (!cells[r * w + c].isEmpty()) {
                    minRow = Math.min(minRow, r);
                    maxRow = Math.max(maxRow, r);
                    minCol = Math.min(minCol, c);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }
        if (maxRow < 0) {
            throw new IllegalArgumentException("at least one input item is required");
        }

        Map<String, Character> letters = new LinkedHashMap<>();
        char next = 'A';
        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                String id = cells[r * w + c];
                if (!id.isEmpty() && !letters.containsKey(id)) {
                    letters.put(id, next++);
                }
            }
        }

        List<String> rows = new ArrayList<>();
        for (int r = minRow; r <= maxRow; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = minCol; c <= maxCol; c++) {
                String id = cells[r * w + c];
                row.append(id.isEmpty() ? ' ' : letters.get(id));
            }
            rows.add(row.toString());
        }
        return new Shape(rows, letters);
    }

    static String safeComment(String name) {
        String cleaned = name == null ? "" : name.trim().replaceAll("[\\r\\n]", " ");
        return cleaned.isEmpty() ? "recipe" : cleaned;
    }
}
