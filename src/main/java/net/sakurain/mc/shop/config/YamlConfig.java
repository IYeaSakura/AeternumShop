package net.sakurain.mc.shop.config;

import net.sakurain.mc.shop.AeternumShop;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class YamlConfig {

    private final AeternumShop plugin;
    private final String fileName;
    private File file;
    private YamlConfiguration config;

    public YamlConfig(AeternumShop plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        setup();
    }

    public void setup() {
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(fileName);
        if (defaultStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)));
        }
    }

    public YamlConfiguration getConfig() {
        if (config == null) {
            setup();
        }
        return config;
    }

    public void save() {
        if (config == null || file == null) {
            return;
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + fileName + ": " + e.getMessage());
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
        InputStream defaultStream = plugin.getResource(fileName);
        if (defaultStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8)));
        }
    }
}
