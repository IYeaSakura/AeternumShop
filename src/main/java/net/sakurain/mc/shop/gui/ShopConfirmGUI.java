package net.sakurain.mc.shop.gui;

import net.sakurain.mc.shop.model.PlayerListing;
import net.sakurain.mc.shop.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopConfirmGUI extends AbstractGUI {

    private final int listingId;
    private final int returnPage;

    public ShopConfirmGUI(Player player, int listingId, int returnPage) {
        super(player, "confirm", 6);
        this.listingId = listingId;
        this.returnPage = returnPage;
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(plugin.getGuiManager().getBorderMaterial());

        Optional<PlayerListing> optional;
        try {
            optional = plugin.getDatabaseManager().getPlayerListingDAO().findById(listingId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load listing: " + e.getMessage());
            return;
        }

        if (optional.isEmpty()) {
            setItem(22, createGuiItem(Material.BARRIER, "<red>挂单已失效"));
            return;
        }

        PlayerListing listing = optional.get();
        Material material = Material.matchMaterial(listing.getItemType());
        if (material == null) material = Material.BARRIER;

        List<String> lore = new ArrayList<>();
        lore.add("<gray>数量: <yellow>" + listing.getItemAmount());
        lore.add("<gray>总价: <yellow>" + listing.getPrice());
        lore.add("<gray>卖家: <yellow>" + listing.getSellerName());

        ItemStack display = createGuiItem(material,
                "<yellow>" + StringUtil.capitalize(listing.getItemType().replace("_", " ")), lore);
        setItem(13, display);

        setItem(29, plugin.getGuiManager().getGuiItem("confirm.confirm"));
        setItem(33, plugin.getGuiManager().getGuiItem("confirm.cancel"));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 29) {
            var result = plugin.getTransactionManager().playerTrade(player, listingId);
            plugin.getMessageManager().send(player, result.getMessageKey(), result.getPlaceholders());
            new ShopPlayerGUI(player, returnPage).open();
        } else if (slot == 33 || slot == 49) {
            new ShopPlayerGUI(player, returnPage).open();
        }
    }
}
