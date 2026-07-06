package net.sakurain.mc.shop.gui;

import net.sakurain.mc.shop.model.TradeRecord;
import net.sakurain.mc.shop.model.TradeType;
import net.sakurain.mc.shop.util.StringUtil;
import net.sakurain.mc.shop.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShopHistoryGUI extends AbstractGUI {

    public ShopHistoryGUI(Player player) {
        super(player, "history", 6);
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(plugin.getGuiManager().getBorderMaterial());

        int limit = plugin.getConfigManager().getInt("player-trade.max-history-records", 20);
        List<TradeRecord> records;
        try {
            records = plugin.getDatabaseManager().getTradeRecordDAO().findRecentByPlayer(player.getUniqueId(), limit);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load trade history: " + e.getMessage());
            return;
        }

        if (records.isEmpty()) {
            setItem(22, createGuiItem(Material.PAPER, "<gray>暂无交易记录"));
        } else {
            int slot = 10;
            for (TradeRecord record : records) {
                if (slot >= 44) break;
                if (slot % 9 == 8) slot += 2;

                Material material = Material.matchMaterial(record.getItemType());
                if (material == null) material = Material.BARRIER;

                String typeName = switch (record.getTradeType()) {
                    case SYSTEM_BUY -> "卖给系统";
                    case SYSTEM_SELL -> "从系统购买";
                    case PLAYER_TRADE -> "玩家交易";
                };

                List<String> lore = new ArrayList<>();
                lore.add("<gray>类型: <yellow>" + typeName);
                lore.add("<gray>物品: <yellow>" + record.getItemType());
                lore.add("<gray>数量: <yellow>" + record.getItemAmount());
                lore.add("<gray>价格: <yellow>" + record.getPrice());
                if (record.getBuyerName() != null) {
                    lore.add("<gray>买家: <yellow>" + record.getBuyerName());
                }
                if (record.getSellerName() != null) {
                    lore.add("<gray>卖家: <yellow>" + record.getSellerName());
                }
                lore.add("<gray>时间: <yellow>" + TimeUtil.format(record.getTradeTime()));

                ItemStack display = createGuiItem(material,
                        "<yellow>" + StringUtil.capitalize(record.getItemType().replace("_", " ")), lore);
                setItem(slot, display);
                slot++;
            }
        }

        setItem(49, plugin.getGuiManager().getGuiItem("navigation.back"));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 49) {
            new ShopMainGUI(player).open();
        }
    }
}
