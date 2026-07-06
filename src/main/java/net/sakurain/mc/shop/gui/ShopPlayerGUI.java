package net.sakurain.mc.shop.gui;

import net.sakurain.mc.shop.model.PlayerListing;
import net.sakurain.mc.shop.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopPlayerGUI extends AbstractGUI {

    private final int page;
    private final Map<Integer, Integer> slotToListingId = new HashMap<>();

    public ShopPlayerGUI(Player player, int page) {
        super(player, "player", 6);
        this.page = page;
        initialize();
    }

    @Override
    public void initialize() {
        slotToListingId.clear();
        fillBorder(plugin.getGuiManager().getBorderMaterial());

        List<PlayerListing> listings;
        try {
            listings = plugin.getDatabaseManager().getPlayerListingDAO().findAllActive();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player listings: " + e.getMessage());
            return;
        }

        int itemsPerPage = 28;
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, listings.size());

        int slot = 10;
        for (int i = start; i < end; i++) {
            if (slot >= 44) break;
            if (slot % 9 == 8) slot += 2;

            PlayerListing listing = listings.get(i);
            Material material = Material.matchMaterial(listing.getItemType());
            if (material == null) material = Material.BARRIER;

            boolean own = listing.getSeller().equals(player.getUniqueId());
            int durationHours = plugin.getConfigManager().getInt("player-trade.listing-duration-hours", 24);
            java.time.LocalDateTime expireTime = listing.getPostTime().plusHours(durationHours);

            List<String> lore = new ArrayList<>();
            lore.add("<gray>数量: <yellow>" + listing.getItemAmount());
            lore.add("<gray>总价: <yellow>" + listing.getPrice() + " <gray>基础货币");
            lore.add("<gray>卖家: <yellow>" + listing.getSellerName());
            lore.add("<gray>剩余时间: <yellow>" + TimeUtil.formatRemaining(expireTime));
            if (own) {
                lore.add("<red>点击下架");
            } else {
                lore.add("<green>点击购买");
            }

            ItemStack display = createGuiItem(material, listing.getItemAmount(), lore);
            setItem(slot, display);
            slotToListingId.put(slot, listing.getId());
            slot++;
        }

        if (page > 0) {
            setItem(45, plugin.getGuiManager().getGuiItem("navigation.previous-page"));
        }
        setItem(49, plugin.getGuiManager().getGuiItem("navigation.back"));
        if (end < listings.size()) {
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
            new ShopPlayerGUI(player, page - 1).open();
            return;
        }
        if (slot == 53) {
            new ShopPlayerGUI(player, page + 1).open();
            return;
        }
        if (slot > 44) return;

        Integer listingId = slotToListingId.get(slot);
        if (listingId == null) return;

        PlayerListing listing;
        try {
            listing = plugin.getDatabaseManager().getPlayerListingDAO().findById(listingId).orElse(null);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load listing: " + e.getMessage());
            return;
        }

        if (listing == null) {
            plugin.getMessageManager().send(player, "listing-not-found");
            initialize();
            player.updateInventory();
            return;
        }

        if (listing.getSeller().equals(player.getUniqueId())) {
            var result = plugin.getTransactionManager().cancelListing(player, listingId);
            plugin.getMessageManager().send(player, result.getMessageKey(), result.getPlaceholders());
            new ShopPlayerGUI(player, page).open();
        } else {
            new ShopConfirmGUI(player, listingId, page).open();
        }
    }

    public int getPage() {
        return page;
    }
}
