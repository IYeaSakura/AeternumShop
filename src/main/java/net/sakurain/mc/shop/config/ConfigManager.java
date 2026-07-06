package net.sakurain.mc.shop.config;

import net.sakurain.mc.shop.AeternumShop;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {

    private final AeternumShop plugin;

    private YamlConfig config;
    private YamlConfig pricesConfig;
    private YamlConfig currencyConfig;
    private YamlConfig messagesConfig;
    private YamlConfig guiConfig;

    public ConfigManager(AeternumShop plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        config = new YamlConfig(plugin, "config.yml");
        pricesConfig = new YamlConfig(plugin, "prices.yml");
        currencyConfig = new YamlConfig(plugin, "currency.yml");
        messagesConfig = new YamlConfig(plugin, "messages.yml");
        guiConfig = new YamlConfig(plugin, "gui.yml");
    }

    public void reloadAll() {
        config.reload();
        pricesConfig.reload();
        currencyConfig.reload();
        messagesConfig.reload();
        guiConfig.reload();
    }

    public YamlConfiguration getConfig() {
        return config.getConfig();
    }

    public YamlConfiguration getPrices() {
        return pricesConfig.getConfig();
    }

    public YamlConfiguration getCurrency() {
        return currencyConfig.getConfig();
    }

    public YamlConfiguration getMessages() {
        return messagesConfig.getConfig();
    }

    public YamlConfiguration getGui() {
        return guiConfig.getConfig();
    }

    public int getInt(String path, int defaultValue) {
        return getConfig().getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return getConfig().getBoolean(path, defaultValue);
    }

    public String getString(String path, String defaultValue) {
        return getConfig().getString(path, defaultValue);
    }
}
