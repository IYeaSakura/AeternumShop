package net.sakurain.mc.shop.command;

import net.sakurain.mc.shop.AeternumShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements TabCompleter {

    private final AeternumShop plugin;

    public CommandManager(AeternumShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> results = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("shop")) {
            if (args.length == 1) {
                if (sender.hasPermission("aeternumshop.command.add")) {
                    results.add("add");
                }
                if (sender.hasPermission("aeternumshop.command.show")) {
                    results.add("show");
                }
                if (sender.hasPermission("aeternumshop.command.history")) {
                    results.add("history");
                }
                if (sender.hasPermission("aeternumshop.command.mailbox")) {
                    results.add("mailbox");
                }
                if (sender.hasPermission("aeternumshop.command.reload")) {
                    results.add("reload");
                }
            }
        }
        return results;
    }
}
