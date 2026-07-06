package net.sakurain.mc.shop.command;

import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.currency.CurrencyBundle;
import net.sakurain.mc.shop.currency.CurrencyUnit;
import net.sakurain.mc.shop.gui.ShopHistoryGUI;
import net.sakurain.mc.shop.gui.ShopMailboxGUI;
import net.sakurain.mc.shop.gui.ShopMainGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopCommand implements CommandExecutor {

    private final AeternumShop plugin;

    public ShopCommand(AeternumShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "player-only");
            return true;
        }

        if (args.length == 0) {
            new ShopMainGUI(player).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "history" -> {
                if (!player.hasPermission("aeternumshop.command.history")) {
                    plugin.getMessageManager().send(player, "no-permission");
                    return true;
                }
                new ShopHistoryGUI(player).open();
            }
            case "reload" -> {
                if (!player.hasPermission("aeternumshop.command.reload")) {
                    plugin.getMessageManager().send(player, "no-permission");
                    return true;
                }
                plugin.reload();
                plugin.getMessageManager().send(player, "reload-success");
            }
            case "mailbox" -> {
                if (!player.hasPermission("aeternumshop.command.mailbox")) {
                    plugin.getMessageManager().send(player, "no-permission");
                    return true;
                }
                new ShopMailboxGUI(player, 0).open();
            }
            case "add" -> handleAdd(player, args);
            case "show" -> handleShow(player);
            default -> new ShopMainGUI(player).open();
        }

        return true;
    }

    private void handleAdd(Player player, String[] args) {
        if (!player.hasPermission("aeternumshop.command.add")) {
            plugin.getMessageManager().send(player, "no-permission");
            return;
        }

        if (args.length < 2) {
            plugin.getMessageManager().send(player, "invalid-price");
            return;
        }

        long price;
        try {
            price = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().send(player, "invalid-price");
            return;
        }

        if (price <= 0) {
            plugin.getMessageManager().send(player, "invalid-price");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().send(player, "hand-empty");
            return;
        }

        var result = plugin.getTransactionManager().createListing(player, item, price);
        plugin.getMessageManager().send(player, result.getMessageKey(), result.getPlaceholders());
    }

    private void handleShow(Player player) {
        CurrencyUnit base = plugin.getCurrencyManager().getBaseCurrency();
        long inventoryValue = plugin.getCurrencyManager().getPlayerBalance(player);

        plugin.getMessageManager().send(player, "currency-show-header");
        plugin.getMessageManager().send(player, "currency-show-base",
                "base_name", base.getDisplayName(),
                "base_material", base.getMaterial().name());
        for (CurrencyBundle bundle : plugin.getCurrencyManager().getBundlesDescending()) {
            plugin.getMessageManager().send(player, "currency-show-bundle",
                    "bundle_name", bundle.getDisplayName(),
                    "bundle_value", String.valueOf(bundle.getBaseValue()),
                    "bundle_material", bundle.getMaterial().name());
        }
        plugin.getMessageManager().send(player, "currency-show-inventory",
                "amount", String.valueOf(inventoryValue));
    }
}
