package net.sakurain.mc.shop.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemUtil {

    public static String getTypeId(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        return item.getType().getKey().getKey().toLowerCase();
    }

    public static String getTypeIdWithNamespace(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        return item.getType().getKey().toString();
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(StringUtil.toComponent(name));
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream().map(StringUtil::toComponent).collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isType(ItemStack item, String typeId) {
        if (item == null || item.getType().isAir() || typeId == null) {
            return false;
        }
        String itemType = getTypeId(item);
        return itemType != null && itemType.equalsIgnoreCase(typeId);
    }

    public static boolean isSimilarEnough(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        return a.getType() == b.getType();
    }
}
