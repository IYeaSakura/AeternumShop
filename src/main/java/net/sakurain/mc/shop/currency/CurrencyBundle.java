package net.sakurain.mc.shop.currency;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CurrencyBundle {

    private final String id;
    private final Material material;
    private final String displayName;
    private final int baseValue;
    private final CurrencyUnit baseUnit;
    private final int maxStackSize;

    public CurrencyBundle(String id, Material material, String displayName, int baseValue, CurrencyUnit baseUnit) {
        this(id, material, displayName, baseValue, baseUnit, material.getMaxStackSize());
    }

    public CurrencyBundle(String id, Material material, String displayName, int baseValue, CurrencyUnit baseUnit, int maxStackSize) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.baseValue = baseValue;
        this.baseUnit = baseUnit;
        this.maxStackSize = maxStackSize;
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public CurrencyUnit getBaseUnit() {
        return baseUnit;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public ItemStack createItem(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName));
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean matches(ItemStack item) {
        return item != null && item.getType() == material;
    }
}
