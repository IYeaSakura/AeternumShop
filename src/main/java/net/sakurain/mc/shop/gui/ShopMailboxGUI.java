package net.sakurain.mc.shop.gui;

import net.sakurain.mc.shop.currency.CurrencyManager;
import net.sakurain.mc.shop.model.MailboxEntry;
import net.sakurain.mc.shop.model.MailboxType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMailboxGUI extends AbstractGUI {

    private final int page;
    private final Map<Integer, Integer> slotToEntryId = new HashMap<>();

    public ShopMailboxGUI(Player player, int page) {
        super(player, "mailbox", 6);
        this.page = page;
        initialize();
    }

    @Override
    public void initialize() {
        slotToEntryId.clear();
        fillBorder(plugin.getGuiManager().getBorderMaterial());

        List<MailboxEntry> entries;
        try {
            entries = plugin.getDatabaseManager().getMailboxDAO().findByPlayer(player.getUniqueId());
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load mailbox: " + e.getMessage());
            return;
        }

        int itemsPerPage = 28;
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, entries.size());

        int slot = 10;
        for (int i = start; i < end; i++) {
            if (slot >= 44) break;
            if (slot % 9 == 8) slot += 2;

            MailboxEntry entry = entries.get(i);

            Material material;
            List<String> lore = new ArrayList<>();
            int amount = 1;

            if (entry.getType() == MailboxType.CURRENCY) {
                material = plugin.getCurrencyManager().getBaseCurrency().getMaterial();
                amount = (int) Math.min(entry.getCurrencyAmount(), material.getMaxStackSize());
                lore.add("<gray>类型: 货币");
                lore.add("<gray>数量: <yellow>" + entry.getCurrencyAmount() + " <gray>基础货币");
            } else {
                material = Material.matchMaterial(entry.getItemType());
                if (material == null) material = Material.BARRIER;
                amount = entry.getItemAmount();
                lore.add("<gray>类型: 物品");
                lore.add("<gray>数量: <yellow>" + entry.getItemAmount());
            }
            lore.add("<green>点击领取");

            setItem(slot, createGuiItem(material, amount, lore));
            slotToEntryId.put(slot, entry.getId());
            slot++;
        }

        if (page > 0) {
            setItem(45, plugin.getGuiManager().getGuiItem("navigation.previous-page"));
        }
        setItem(49, plugin.getGuiManager().getGuiItem("navigation.back"));
        if (end < entries.size()) {
            setItem(53, plugin.getGuiManager().getGuiItem("navigation.next-page"));
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 49) {
            new ShopMainGUI(player).open();
            return;
        }
        if (slot == 45 && page > 0) {
            new ShopMailboxGUI(player, page - 1).open();
            return;
        }
        if (slot == 53) {
            new ShopMailboxGUI(player, page + 1).open();
            return;
        }
        if (slot > 44) return;

        Integer entryId = slotToEntryId.get(slot);
        if (entryId == null) return;

        MailboxEntry entry;
        try {
            List<MailboxEntry> entries = plugin.getDatabaseManager().getMailboxDAO().findByPlayer(player.getUniqueId());
            entry = entries.stream().filter(e -> e.getId() == entryId).findFirst().orElse(null);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load mailbox entry: " + e.getMessage());
            return;
        }

        if (entry == null) return;

        if (entry.getType() == MailboxType.CURRENCY) {
            CurrencyManager cm = plugin.getCurrencyManager();
            long remaining = cm.deposit(player, entry.getCurrencyAmount());
            if (remaining > 0) {
                plugin.getMessageManager().send(player, "inventory-full");
                return;
            }
        } else {
            Material material = Material.matchMaterial(entry.getItemType());
            if (material == null) return;
            if (!player.getInventory().addItem(new ItemStack(material, entry.getItemAmount())).isEmpty()) {
                plugin.getMessageManager().send(player, "inventory-full");
                return;
            }
        }

        try {
            plugin.getDatabaseManager().getMailboxDAO().markReceived(entryId);
            plugin.getMessageManager().send(player, "mailbox-receive");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to mark mailbox received: " + e.getMessage());
        }

        new ShopMailboxGUI(player, page).open();
    }
}
