package net.sakurain.mc.shop.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    public static boolean canHold(Player player, Material material, int amount) {
        return getFreeSpace(player, material) >= amount;
    }

    public static int getFreeSpace(Player player, Material material) {
        int space = 0;
        int maxStack = material.getMaxStackSize();

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                space += maxStack;
            } else if (item.getType() == material && item.getAmount() < maxStack) {
                space += maxStack - item.getAmount();
            }
        }
        return space;
    }

    public static int getEmptySlots(Player player) {
        int empty = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                empty++;
            }
        }
        return empty;
    }

    public static int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static void removeItems(Player player, Material material, int amount) {
        Inventory inv = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == material) {
                int remove = Math.min(remaining, item.getAmount());
                item.setAmount(item.getAmount() - remove);
                if (item.getAmount() <= 0) {
                    inv.setItem(i, null);
                }
                remaining -= remove;
            }
        }
    }
}
