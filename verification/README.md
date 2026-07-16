# Generator verification

`CraftingScriptGenerator` is pure Java with no Minecraft dependencies, so it is checked with a
standalone harness instead of a Gradle/JUnit test (ForgeGradle's modular FML runtime breaks the
plain JUnit test worker).

Run from the project root (`EasyRecipesMod/`):

```powershell
javac -d verification/out src/main/java/com/example/easyrecipes/script/CraftingScriptGenerator.java verification/Driver.java
java -cp verification/out com.example.easyrecipes.script.Driver
```

Expected: `7 passed, 0 failed`.
