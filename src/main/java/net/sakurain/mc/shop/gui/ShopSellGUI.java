package net.sakurain.mc.shop.gui;

import net.sakurain.mc.shop.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopSellGUI extends AbstractGUI {

    private final Map<Integer, String> slotToItem = new HashMap<>();
    private final Map<Integer, Integer> slotToAmount = new HashMap<>();
    private final Map<Integer, Long> slotToCost = new HashMap<>();

    public ShopSellGUI(Player player) {
        super(player, "sell", 6);
        initialize();
    }

    @Override
    public void initialize() {
        slotToItem.clear();
        slotToAmount.clear();
        slotToCost.clear();
        fillBorder(plugin.getGuiManager().getBorderMaterial());

        ConfigurationSection sellSection = plugin.getConfigManager().getPrices().getConfigurationSection("sell");
        if (sellSection == null) return;

        int slot = 10;
        for (String key : sellSection.getKeys(false)) {
            if (slot >= 44) break;
            if (slot % 9 == 8) slot += 2;

            Material material = Material.matchMaterial(key);
            if (material == null) continue;

            int amount = sellSection.getInt(key + ".amount", 1);
            long cost = sellSection.getLong(key + ".cost", 0);

            List<String> lore = new ArrayList<>();
            lore.add("<gray>购买数量: <yellow>" + amount);
            lore.add("<gray>花费货币: <yellow>" + cost);
            lore.add("<green>点击购买");

            ItemStack display = createGuiItem(material, "<yellow>" + StringUtil.capitalize(key), lore);
            setItem(slot, display);
            slotToItem.put(slot, key);
            slotToAmount.put(slot, amount);
            slotToCost.put(slot, cost);
            slot++;
        }

        setItem(49, plugin.getGuiManager().getGuiItem("navigation.back"));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 49) {
            new ShopMainGUI(player).open();
            return;
        }

        String itemType = slotToItem.get(slot);
        if (itemType == null) return;

        int amount = slotToAmount.get(slot);
        long cost = slotToCost.get(slot);

        var result = plugin.getTransactionManager().systemSell(player, itemType, amount, cost);
        plugin.getMessageManager().send(player, result.getMessageKey(), result.getPlaceholders());
        if (result.isSuccess()) {
            initialize();
            player.updateInventory();
        }
    }
}
