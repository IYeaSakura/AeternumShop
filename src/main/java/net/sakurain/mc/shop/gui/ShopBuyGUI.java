package net.sakurain.mc.shop.gui;

import net.sakurain.mc.shop.util.ItemUtil;
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

public class ShopBuyGUI extends AbstractGUI {

    private final Map<Integer, String> slotToItem = new HashMap<>();
    private final Map<Integer, Integer> slotToAmount = new HashMap<>();
    private final Map<Integer, Long> slotToReward = new HashMap<>();

    public ShopBuyGUI(Player player) {
        super(player, "buy", 6);
        initialize();
    }

    @Override
    public void initialize() {
        slotToItem.clear();
        slotToAmount.clear();
        slotToReward.clear();
        fillBorder(plugin.getGuiManager().getBorderMaterial());

        ConfigurationSection buySection = plugin.getConfigManager().getPrices().getConfigurationSection("buy");
        if (buySection == null) return;

        int slot = 10;
        for (String key : buySection.getKeys(false)) {
            if (slot >= 44) break;
            if (slot % 9 == 8) slot += 2;

            Material material = Material.matchMaterial(key);
            if (material == null) continue;

            int amount = buySection.getInt(key + ".amount", 1);
            long reward = buySection.getLong(key + ".reward", 0);

            List<String> lore = new ArrayList<>();
            lore.add("<gray>出售数量: <yellow>" + amount);
            lore.add("<gray>获得货币: <yellow>" + reward);
            lore.add("<green>点击出售");

            ItemStack display = createGuiItem(material, "<yellow>" + StringUtil.capitalize(key), lore);
            setItem(slot, display);
            slotToItem.put(slot, key);
            slotToAmount.put(slot, amount);
            slotToReward.put(slot, reward);
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
        long reward = slotToReward.get(slot);

        var result = plugin.getTransactionManager().systemBuy(player, itemType, amount, reward);
        plugin.getMessageManager().send(player, result.getMessageKey(), result.getPlaceholders());
        if (result.isSuccess()) {
            initialize();
            player.updateInventory();
        }
    }
}
