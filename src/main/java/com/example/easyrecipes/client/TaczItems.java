package com.example.easyrecipes.client;

import com.example.easyrecipes.script.CraftingScriptGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * TACZ packs every gun (and attachment/ammo) into a single item id — {@code tacz:modern_kinetic_gun}
 * — and keeps the real identity in NBT ({@code GunId}). A plain registry id would therefore mean
 * "any gun" and craft an empty one. TACZ ships a KubeJS binding for exactly this case, so we emit
 * {@code TimelessItem.of('<namespace>:gun/<path>')} instead.
 *
 * <p>Read purely off the NBT tags, so no compile dependency on TACZ. Gun packs work for free: the
 * namespace comes from the gun's own id, not from a hardcoded "tacz".
 */
public final class TaczItems {

    private static final String DEFAULT_GUN = "tacz:modern_kinetic_gun";
    private static final String ATTACHMENT = "tacz:attachment";
    private static final String AMMO = "tacz:ammo";

    private TaczItems() {}

    /** A raw-JS token for a TACZ item, or null if this is an ordinary item. */
    public static String token(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String itemId = item == null ? "" : item.toString();

        // Guns can be custom items (KubeJS "tacz_gun" type), so identify them by the tag alone.
        if (tag.contains("GunId", Tag.TAG_STRING)) {
            return build(itemId, tag.getString("GunId"), "gun", DEFAULT_GUN);
        }
        if (itemId.equals(ATTACHMENT) && tag.contains("AttachmentId", Tag.TAG_STRING)) {
            return build(itemId, tag.getString("AttachmentId"), "attachment", ATTACHMENT);
        }
        // tacz:ammo_box carries an AmmoId too, so only the ammo item itself qualifies.
        if (itemId.equals(AMMO) && tag.contains("AmmoId", Tag.TAG_STRING)) {
            return build(itemId, tag.getString("AmmoId"), "ammo", AMMO);
        }
        return null;
    }

    /**
     * Stores the item as data, not as finished JS: the generator needs a different call for an
     * output (build the gun) than for an input (match any gun of that kind). Keeping the real
     * item id matters for addons that register their own gun item.
     */
    private static String build(String itemId, String rawId, String type, String defaultItem) {
        ResourceLocation id = ResourceLocation.tryParse(rawId);
        if (id == null) {
            return null;
        }
        String item = itemId.isEmpty() ? defaultItem : itemId;
        return CraftingScriptGenerator.TACZ_PREFIX + type + "|" + id + "|" + item;
    }
}
