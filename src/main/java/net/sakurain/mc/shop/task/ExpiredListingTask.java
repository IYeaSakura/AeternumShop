package net.sakurain.mc.shop.task;

import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.model.ListingStatus;
import net.sakurain.mc.shop.model.MailboxEntry;
import net.sakurain.mc.shop.model.MailboxType;
import net.sakurain.mc.shop.model.PlayerListing;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ExpiredListingTask extends BukkitRunnable {

    private final AeternumShop plugin;

    public ExpiredListingTask(AeternumShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int durationHours = plugin.getConfigManager().getInt("player-trade.listing-duration-hours", 24);
        LocalDateTime expireBefore = LocalDateTime.now().minusHours(durationHours);

        try {
            List<PlayerListing> expired = plugin.getDatabaseManager().getPlayerListingDAO().findExpired(expireBefore);
            for (PlayerListing listing : expired) {
                MailboxEntry entry = new MailboxEntry(
                        listing.getSeller(),
                        MailboxType.ITEM,
                        listing.getItemType(),
                        listing.getItemAmount(),
                        0,
                        false,
                        LocalDateTime.now()
                );
                plugin.getDatabaseManager().getMailboxDAO().insert(entry);
                plugin.getDatabaseManager().getPlayerListingDAO().updateStatus(listing.getId(), ListingStatus.EXPIRED);

                org.bukkit.entity.Player seller = plugin.getServer().getPlayer(listing.getSeller());
                if (seller != null && seller.isOnline()) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            plugin.getMessageManager().send(seller, "listing-expired"));
                }
            }
            if (!expired.isEmpty()) {
                plugin.getLogger().info("Expired " + expired.size() + " listings.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to process expired listings: " + e.getMessage());
        }
    }
}
