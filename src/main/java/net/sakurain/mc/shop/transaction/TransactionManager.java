package net.sakurain.mc.shop.transaction;

import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.currency.CurrencyManager;
import net.sakurain.mc.shop.database.DatabaseManager;
import net.sakurain.mc.shop.database.dao.MailboxDAO;
import net.sakurain.mc.shop.database.dao.PlayerListingDAO;
import net.sakurain.mc.shop.database.dao.TradeRecordDAO;
import net.sakurain.mc.shop.model.ListingStatus;
import net.sakurain.mc.shop.model.MailboxEntry;
import net.sakurain.mc.shop.model.MailboxType;
import net.sakurain.mc.shop.model.PlayerListing;
import net.sakurain.mc.shop.model.TradeRecord;
import net.sakurain.mc.shop.model.TradeType;
import net.sakurain.mc.shop.util.InventoryUtil;
import net.sakurain.mc.shop.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class TransactionManager {

    private final AeternumShop plugin;
    private final CurrencyManager currencyManager;
    private final DatabaseManager databaseManager;
    private final PlayerListingDAO listingDAO;
    private final TradeRecordDAO tradeRecordDAO;
    private final MailboxDAO mailboxDAO;

    public TransactionManager(AeternumShop plugin) {
        this.plugin = plugin;
        this.currencyManager = plugin.getCurrencyManager();
        this.databaseManager = plugin.getDatabaseManager();
        this.listingDAO = databaseManager.getPlayerListingDAO();
        this.tradeRecordDAO = databaseManager.getTradeRecordDAO();
        this.mailboxDAO = databaseManager.getMailboxDAO();
    }

    public TransactionResult systemBuy(Player player, String itemType, int amount, long reward) {
        Material material = Material.matchMaterial(itemType);
        if (material == null) {
            return TransactionResult.fail("listing-not-found");
        }

        int available = InventoryUtil.countItems(player, material);
        if (available < amount) {
            return TransactionResult.fail("inventory-full", "amount", String.valueOf(amount));
        }

        InventoryUtil.removeItems(player, material, amount);
        long mailbox = currencyManager.deposit(player, reward);

        recordTrade(TradeType.SYSTEM_BUY, null, null, player.getUniqueId(), player.getName(), itemType, amount, reward);

        String messageKey = mailbox > 0 ? "buy-success-mailbox" : "buy-success";
        return TransactionResult.success(messageKey,
                "amount", String.valueOf(amount),
                "item", material.name().toLowerCase(),
                "reward", String.valueOf(reward),
                "mailbox", String.valueOf(mailbox));
    }

    public TransactionResult systemBuyMax(Player player, String itemType, int amountPerTrade, long rewardPerTrade) {
        Material material = Material.matchMaterial(itemType);
        if (material == null) {
            return TransactionResult.fail("listing-not-found");
        }

        int available = InventoryUtil.countItems(player, material);
        if (available < amountPerTrade) {
            return TransactionResult.fail("inventory-full", "amount", String.valueOf(amountPerTrade));
        }

        int maxSets = available / amountPerTrade;
        int totalAmount = maxSets * amountPerTrade;
        long totalReward = maxSets * rewardPerTrade;

        InventoryUtil.removeItems(player, material, totalAmount);
        long mailbox = currencyManager.deposit(player, totalReward);

        recordTrade(TradeType.SYSTEM_BUY, null, null, player.getUniqueId(), player.getName(), itemType, totalAmount, totalReward);

        String messageKey = mailbox > 0 ? "buy-success-mailbox" : "buy-success";
        return TransactionResult.success(messageKey,
                "amount", String.valueOf(totalAmount),
                "item", material.name().toLowerCase(),
                "reward", String.valueOf(totalReward),
                "mailbox", String.valueOf(mailbox));
    }

    public TransactionResult systemSell(Player player, String itemType, int amount, long cost) {
        Material material = Material.matchMaterial(itemType);
        if (material == null) {
            return TransactionResult.fail("listing-not-found");
        }

        if (currencyManager.getPlayerBalance(player) < cost) {
            return TransactionResult.fail("not-enough-currency", "amount", String.valueOf(cost));
        }

        if (!InventoryUtil.canHold(player, material, amount)) {
            return TransactionResult.fail("inventory-full");
        }

        if (!currencyManager.withdraw(player, cost)) {
            return TransactionResult.fail("not-enough-currency", "amount", String.valueOf(cost));
        }

        player.getInventory().addItem(new ItemStack(material, amount));
        recordTrade(TradeType.SYSTEM_SELL, player.getUniqueId(), player.getName(), null, null, itemType, amount, cost);

        return TransactionResult.success("sell-success",
                "amount", String.valueOf(amount),
                "item", itemType,
                "cost", String.valueOf(cost));
    }

    public TransactionResult systemSellMax(Player player, String itemType, int amountPerTrade, long costPerTrade) {
        Material material = Material.matchMaterial(itemType);
        if (material == null) {
            return TransactionResult.fail("listing-not-found");
        }

        long balance = currencyManager.getPlayerBalance(player);
        if (balance < costPerTrade) {
            return TransactionResult.fail("not-enough-currency", "amount", String.valueOf(costPerTrade));
        }

        int maxByBalance = (int) (balance / costPerTrade);
        int maxByInventory = InventoryUtil.getFreeSpace(player, material) / amountPerTrade;
        int maxSets = Math.min(maxByBalance, maxByInventory);

        if (maxSets <= 0) {
            return TransactionResult.fail(maxByBalance <= 0 ? "not-enough-currency" : "inventory-full");
        }

        int totalAmount = maxSets * amountPerTrade;
        long totalCost = maxSets * costPerTrade;

        if (!currencyManager.withdraw(player, totalCost)) {
            return TransactionResult.fail("not-enough-currency", "amount", String.valueOf(totalCost));
        }

        player.getInventory().addItem(new ItemStack(material, totalAmount));
        recordTrade(TradeType.SYSTEM_SELL, player.getUniqueId(), player.getName(), null, null, itemType, totalAmount, totalCost);

        return TransactionResult.success("sell-success",
                "amount", String.valueOf(totalAmount),
                "item", itemType,
                "cost", String.valueOf(totalCost));
    }

    public TransactionResult playerTrade(Player buyer, int listingId) {
        Optional<PlayerListing> optional;
        try {
            optional = listingDAO.findById(listingId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load listing: " + e.getMessage());
            return TransactionResult.fail("listing-not-found");
        }

        if (optional.isEmpty()) {
            return TransactionResult.fail("listing-not-found");
        }

        PlayerListing listing = optional.get();

        if (listing.getSeller().equals(buyer.getUniqueId())) {
            return TransactionResult.fail("cannot-buy-self");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            return TransactionResult.fail("listing-not-found");
        }

        if (currencyManager.getPlayerBalance(buyer) < listing.getPrice()) {
            return TransactionResult.fail("not-enough-currency", "amount", String.valueOf(listing.getPrice()));
        }

        Material material = Material.matchMaterial(listing.getItemType());
        if (material == null) {
            return TransactionResult.fail("listing-not-found");
        }

        if (!InventoryUtil.canHold(buyer, material, listing.getItemAmount())) {
            return TransactionResult.fail("inventory-full");
        }

        if (!currencyManager.withdraw(buyer, listing.getPrice())) {
            return TransactionResult.fail("not-enough-currency", "amount", String.valueOf(listing.getPrice()));
        }

        buyer.getInventory().addItem(new ItemStack(material, listing.getItemAmount()));

        // 给卖家货币
        Player seller = Bukkit.getPlayer(listing.getSeller());
        if (seller != null && seller.isOnline()) {
            currencyManager.deposit(seller, listing.getPrice());
        } else {
            // 存入信箱，自动合并已有货币条目
            try {
                mailboxDAO.depositCurrency(listing.getSeller(), listing.getPrice());
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to deposit seller currency to mailbox: " + e.getMessage());
            }
        }

        try {
            listingDAO.updateStatus(listing.getId(), ListingStatus.SOLD);
            recordTrade(TradeType.PLAYER_TRADE, buyer.getUniqueId(), buyer.getName(),
                    listing.getSeller(), listing.getSellerName(), listing.getItemType(),
                    listing.getItemAmount(), listing.getPrice());
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update listing status: " + e.getMessage());
        }

        return TransactionResult.success("trade-success",
                "seller", listing.getSellerName(),
                "item", listing.getItemType());
    }

    public TransactionResult cancelListing(Player seller, int listingId) {
        Optional<PlayerListing> optional;
        try {
            optional = listingDAO.findById(listingId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load listing: " + e.getMessage());
            return TransactionResult.fail("listing-not-found");
        }

        if (optional.isEmpty()) {
            return TransactionResult.fail("listing-not-found");
        }

        PlayerListing listing = optional.get();
        if (!listing.getSeller().equals(seller.getUniqueId())) {
            return TransactionResult.fail("not-your-listing");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            return TransactionResult.fail("listing-not-found");
        }

        Material material = Material.matchMaterial(listing.getItemType());
        if (material != null) {
            MailboxEntry entry = new MailboxEntry(
                    seller.getUniqueId(),
                    MailboxType.ITEM,
                    listing.getItemType(),
                    listing.getItemAmount(),
                    0,
                    false,
                    LocalDateTime.now()
            );
            try {
                mailboxDAO.insert(entry);
                listingDAO.updateStatus(listing.getId(), ListingStatus.CANCELLED);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to cancel listing: " + e.getMessage());
                return TransactionResult.fail("listing-not-found");
            }
        }

        return TransactionResult.success("cancel-success");
    }

    public TransactionResult createListing(Player seller, ItemStack item, long price) {
        if (item == null || item.getType().isAir()) {
            return TransactionResult.fail("hand-empty");
        }

        int maxListings = plugin.getConfigManager().getInt("player-trade.max-listings-per-player", 5);
        try {
            if (listingDAO.countActiveBySeller(seller.getUniqueId()) >= maxListings) {
                return TransactionResult.fail("max-listings", "max", String.valueOf(maxListings));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to count listings: " + e.getMessage());
        }

        String itemType = ItemUtil.getTypeId(item);
        int amount = item.getAmount();

        PlayerListing listing = new PlayerListing(
                seller.getUniqueId(),
                seller.getName(),
                itemType,
                amount,
                price,
                LocalDateTime.now(),
                ListingStatus.ACTIVE
        );

        try {
            listingDAO.insert(listing);
            item.setAmount(0);
            seller.getInventory().setItemInMainHand(null);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create listing: " + e.getMessage());
            return TransactionResult.fail("listing-not-found");
        }

        return TransactionResult.success("listing-success",
                "amount", String.valueOf(amount),
                "item", itemType,
                "price", String.valueOf(price));
    }

    private void recordTrade(TradeType type, UUID buyer, String buyerName, UUID seller, String sellerName,
                             String itemType, int amount, long price) {
        TradeRecord record = new TradeRecord(
                type, buyer, buyerName, seller, sellerName,
                itemType, amount, price, LocalDateTime.now()
        );
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                tradeRecordDAO.insert(record);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to record trade: " + e.getMessage());
            }
        });
    }
}
