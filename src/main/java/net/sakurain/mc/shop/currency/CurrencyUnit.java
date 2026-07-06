package net.sakurain.mc.shop.currency;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CurrencyUnit {

    private final String id;
    private final Material material;
    private final String displayName;
    private final int maxStackSize;

    public CurrencyUnit(String id, Material material, String displayName) {
        this(id, material, displayName, material.getMaxStackSize());
    }

    public CurrencyUnit(String id, Material material, String displayName, int maxStackSize) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
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
