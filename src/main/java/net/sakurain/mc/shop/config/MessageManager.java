package net.sakurain.mc.shop.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageManager {

    private final ConfigManager configManager;
    private final MiniMessage miniMessage;

    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public Component getMessage(String key, String... placeholders) {
        String prefix = configManager.getMessages().getString("prefix", "");
        String raw = configManager.getMessages().getString("messages." + key,
                configManager.getMessages().getString(key, "<red>Missing message: " + key));
        String message = applyPlaceholders(prefix + raw, placeholders);
        return miniMessage.deserialize(message);
    }

    public Component getRaw(String key, String... placeholders) {
        String raw = configManager.getMessages().getString(key, "");
        String message = applyPlaceholders(raw, placeholders);
        return miniMessage.deserialize(message);
    }

    private String applyPlaceholders(String message, String... placeholders) {
        String result = message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "%" + placeholders[i] + "%";
                result = result.replace(placeholder, placeholders[i + 1]);
            }
        }
        return result;
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(getMessage(key, placeholders));
    }

    public void send(Player player, String key, String... placeholders) {
        player.sendMessage(getMessage(key, placeholders));
    }

    public Component getTitle(String key) {
        return miniMessage.deserialize(configManager.getMessages().getString("shop." + key + "-title", ""));
    }
}
