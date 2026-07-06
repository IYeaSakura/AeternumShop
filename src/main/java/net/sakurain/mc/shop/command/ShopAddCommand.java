package net.sakurain.mc.shop.command;

import net.sakurain.mc.shop.AeternumShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopAddCommand implements CommandExecutor {

    private final AeternumShop plugin;

    public ShopAddCommand(AeternumShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "player-only");
            return true;
        }

        if (args.length < 1) {
            plugin.getMessageManager().send(player, "invalid-price");
            return true;
        }

        long price;
        try {
            price = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().send(player, "invalid-price");
            return true;
        }

        if (price <= 0) {
            plugin.getMessageManager().send(player, "invalid-price");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().send(player, "hand-empty");
            return true;
        }

        var result = plugin.getTransactionManager().createListing(player, item, price);
        plugin.getMessageManager().send(player, result.getMessageKey(), result.getPlaceholders());
        return true;
    }
}
