package com.example.easyrecipes.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

/** Reads the fluid id contained in a bucket / fluid-container item (client-side). */
public final class Fluids {

    private Fluids() {}

    /** e.g. a {@code minecraft:water_bucket} stack → {@code "minecraft:water"}; "" if none. */
    public static String fluidId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        return FluidUtil.getFluidContained(stack).map(fs -> {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fs.getFluid());
            return id == null ? "" : id.toString();
        }).orElse("");
    }
}
