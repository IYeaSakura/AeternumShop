package net.sakurain.mc.shop.listener;

import net.sakurain.mc.shop.AeternumShop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class PlayerJoinListener implements Listener {

    private final AeternumShop plugin;

    public PlayerJoinListener(AeternumShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().getBoolean("features.mailbox", true)) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int count = plugin.getDatabaseManager().getMailboxDAO()
                        .countByPlayer(event.getPlayer().getUniqueId());
                if (count > 0) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            plugin.getMessageManager().send(event.getPlayer(), "mailbox-notify"));
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to check mailbox on join: " + e.getMessage());
            }
        });
    }
}
