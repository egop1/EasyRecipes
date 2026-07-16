# Easy Recipes (KubeJS)

Make Minecraft recipes in-game instead of writing JavaScript by hand.

Minecraft **1.20.1** · **Forge** · requires **[KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs)**

---

## What it does

Run `/easyrecipes` and pick a station — that opens its recipe list. Press **Add** for an empty
editor, drag items into the slots, and press **Save**. The mod writes the KubeJS script for you,
reloads it, and the recipe is live — no files, no syntax errors, no restart.

You can also type an existing recipe id (with Tab-completion) to **override** or **delete** it, so
"make diamonds cheaper" or "remove TNT" takes about ten seconds.

Recipes are stored on the **server**, so everything you make applies to everyone on it.

## Supported stations

**Vanilla** — crafting table (shaped + shapeless), furnace, blast furnace, smoker, campfire,
stonecutter, smithing table.

**[Create](https://www.curseforge.com/minecraft/mc-mods/create)** — crushing wheels, millstone,
mechanical press, mechanical saw, encased fan (roasting / smelting / washing / haunting), sandpaper
polishing, deployer, spout, item drain, mechanical mixer, compacting, and the mechanical crafter at
**any grid size up to 9×9**.

> Create stations also need **[KubeJS Create](https://www.curseforge.com/minecraft/mc-mods/kubejs-create)**,
> the addon that teaches KubeJS about Create's recipes — Create by itself is not enough. The
> stations stay greyed out until both are installed.

Machines with random results let you build a weighted output list with a chance per item. Fluids are
set by dropping a bucket into the fluid slot and typing an amount in mB.

## Mod compatibility

**[TACZ](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero)** guns work.
All TACZ guns share one item id and keep their identity in NBT, so a plain id would mean "any gun"
and craft an empty one. Easy Recipes reads the gun's NBT id and emits TACZ's own KubeJS builder
instead — for both crafting *a* gun and crafting *from* one. Gun packs and addons with their own gun
items are handled too.

## Installing

Put the jar on **both the client and the server**, alongside KubeJS. The GUI lives on the client, but
the server owns the script files — that is why it is needed on both sides.

Editing recipes requires **operator permission** (level 2).

## Where things end up

| Path | What |
| --- | --- |
| `kubejs/server_scripts/easyrecipes_generated.js` | the generated script — regenerated on every change |
| `config/easyrecipes/recipes.json` | the recipes you made; this file is the source of truth |

The script is generated, so do not edit it by hand — edit recipes in the GUI instead. The manifest is
the thing worth backing up.

## Building

Requires JDK 17.

```
gradlew build          # -> build/libs/easyrecipes-1.0.0.jar
```

Recipe generation is pure Java with no Minecraft types, so it is verified by a standalone harness
rather than a Gradle test (ForgeGradle's modular runtime breaks the plain JUnit worker):

```
javac -d verification/out src/main/java/com/example/easyrecipes/script/CraftingScriptGenerator.java src/main/java/com/example/easyrecipes/script/RecipeScriptGenerator.java src/main/java/com/example/easyrecipes/data/RecipeEntry.java verification/Driver.java
java -cp verification/out com.example.easyrecipes.script.Driver
```

## Not supported yet

- Sequenced assembly (Create) — needs its own multi-step editor.
- Brewing — vanilla brewing is not data-driven and KubeJS has no server-script API for it.
- Editing a mechanical crafter recipe does not read the original's grid size; you pick it yourself.

## License

MIT — see [LICENSE](LICENSE).
