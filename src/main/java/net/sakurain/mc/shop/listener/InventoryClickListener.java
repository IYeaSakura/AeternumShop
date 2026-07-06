package net.sakurain.mc.shop.listener;

import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.gui.AbstractGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryClickListener implements Listener {

    private final AeternumShop plugin;

    public InventoryClickListener(AeternumShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof AbstractGUI gui)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Only handle clicks inside the top (GUI) inventory
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getInventory().getSize()) {
            return;
        }

        plugin.getGuiManager().setOpenGui(player, gui);
        try {
            gui.onClick(event);
        } catch (Exception e) {
            plugin.getLogger().warning("GUI click handler error in " + gui.getClass().getSimpleName()
                    + " at slot " + event.getSlot() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
