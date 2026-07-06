package net.sakurain.mc.shop.command;

import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.gui.ShopHistoryGUI;
import net.sakurain.mc.shop.gui.ShopMailboxGUI;
import net.sakurain.mc.shop.gui.ShopMainGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            default -> new ShopMainGUI(player).open();
        }

        return true;
    }
}
