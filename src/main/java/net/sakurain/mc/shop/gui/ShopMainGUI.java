package net.sakurain.mc.shop.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopMainGUI extends AbstractGUI {

    public ShopMainGUI(Player player) {
        super(player, "main", 6);
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(plugin.getGuiManager().getBorderMaterial());

        GUIManager gui = plugin.getGuiManager();
        setItem(gui.getGuiSlot("main-menu.system-buy"), gui.getGuiItem("main-menu.system-buy"));
        setItem(gui.getGuiSlot("main-menu.system-sell"), gui.getGuiItem("main-menu.system-sell"));
        setItem(gui.getGuiSlot("main-menu.player-trade"), gui.getGuiItem("main-menu.player-trade"));
        setItem(gui.getGuiSlot("main-menu.mailbox"), gui.getGuiItem("main-menu.mailbox"));
        setItem(gui.getGuiSlot("main-menu.close"), gui.getGuiItem("main-menu.close"));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        GUIManager gui = plugin.getGuiManager();

        if (slot == gui.getGuiSlot("main-menu.system-buy")) {
            new ShopBuyGUI(player).open();
        } else if (slot == gui.getGuiSlot("main-menu.system-sell")) {
            new ShopSellGUI(player).open();
        } else if (slot == gui.getGuiSlot("main-menu.player-trade")) {
            new ShopPlayerGUI(player, 0).open();
        } else if (slot == gui.getGuiSlot("main-menu.mailbox")) {
            new ShopMailboxGUI(player, 0).open();
        } else if (slot == gui.getGuiSlot("main-menu.close")) {
            player.closeInventory();
        }
    }
}
