package net.sakurain.mc.shop.listener;

import net.sakurain.mc.shop.gui.AbstractGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryDragListener implements Listener {

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AbstractGUI) {
            event.setCancelled(true);
        }
    }
}
