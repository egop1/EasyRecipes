package com.example.easyrecipes;

import com.example.easyrecipes.menu.CookingMenu;
import com.example.easyrecipes.menu.CraftingMenu;
import com.example.easyrecipes.menu.CreateProcessingMenu;
import com.example.easyrecipes.menu.DeployingMenu;
import com.example.easyrecipes.menu.EmptyingMenu;
import com.example.easyrecipes.menu.FillingMenu;
import com.example.easyrecipes.menu.MechanicalCraftingMenu;
import com.example.easyrecipes.menu.MixingMenu;
import com.example.easyrecipes.menu.SmithingMenu;
import com.example.easyrecipes.menu.StonecutterMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registry holder. Editors are menu-backed (ghost slots); the picker and list are plain screens. */
public final class Registration {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EasyRecipes.MOD_ID);

    public static final RegistryObject<MenuType<CraftingMenu>> CRAFTING_MENU =
            MENUS.register("crafting", () -> IForgeMenuType.create(CraftingMenu::new));

    public static final RegistryObject<MenuType<CookingMenu>> COOKING_MENU =
            MENUS.register("cooking", () -> IForgeMenuType.create(CookingMenu::new));

    public static final RegistryObject<MenuType<StonecutterMenu>> STONECUTTER_MENU =
            MENUS.register("stonecutter", () -> IForgeMenuType.create(StonecutterMenu::new));

    public static final RegistryObject<MenuType<SmithingMenu>> SMITHING_MENU =
            MENUS.register("smithing", () -> IForgeMenuType.create(SmithingMenu::new));

    public static final RegistryObject<MenuType<CreateProcessingMenu>> CREATE_PROCESSING_MENU =
            MENUS.register("create_processing", () -> IForgeMenuType.create(CreateProcessingMenu::new));

    public static final RegistryObject<MenuType<DeployingMenu>> DEPLOYING_MENU =
            MENUS.register("deploying", () -> IForgeMenuType.create(DeployingMenu::new));

    public static final RegistryObject<MenuType<FillingMenu>> FILLING_MENU =
            MENUS.register("filling", () -> IForgeMenuType.create(FillingMenu::new));

    public static final RegistryObject<MenuType<EmptyingMenu>> EMPTYING_MENU =
            MENUS.register("emptying", () -> IForgeMenuType.create(EmptyingMenu::new));

    public static final RegistryObject<MenuType<MixingMenu>> MIXING_MENU =
            MENUS.register("mixing", () -> IForgeMenuType.create(MixingMenu::new));

    public static final RegistryObject<MenuType<MechanicalCraftingMenu>> MECHANICAL_CRAFTING_MENU =
            MENUS.register("mechanical_crafting", () -> IForgeMenuType.create(MechanicalCraftingMenu::new));

    private Registration() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
