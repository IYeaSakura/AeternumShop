package net.sakurain.mc.shop;

import net.sakurain.mc.shop.command.CommandManager;
import net.sakurain.mc.shop.command.ShopAddCommand;
import net.sakurain.mc.shop.command.ShopCommand;
import net.sakurain.mc.shop.config.ConfigManager;
import net.sakurain.mc.shop.config.MessageManager;
import net.sakurain.mc.shop.currency.CurrencyManager;
import net.sakurain.mc.shop.database.DatabaseManager;
import net.sakurain.mc.shop.gui.GUIManager;
import net.sakurain.mc.shop.listener.InventoryClickListener;
import net.sakurain.mc.shop.listener.InventoryDragListener;
import net.sakurain.mc.shop.listener.PlayerJoinListener;
import net.sakurain.mc.shop.task.ExpiredListingTask;
import net.sakurain.mc.shop.transaction.TransactionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AeternumShop extends JavaPlugin {

    private static AeternumShop instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private CurrencyManager currencyManager;
    private TransactionManager transactionManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("currency.yml", false);
        saveResource("prices.yml", false);
        saveResource("messages.yml", false);
        saveResource("gui.yml", false);

        // 1. Config
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();
        this.messageManager = new MessageManager(this.configManager);

        // 2. Database
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();

        // 3. Currency
        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.loadFromConfig(configManager.getCurrency());

        // 4. Transaction
        this.transactionManager = new TransactionManager(this);

        // 5. GUI
        this.guiManager = new GUIManager(this);

        // 6. Commands
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("shopadd").setExecutor(new ShopAddCommand(this));
        getCommand("shop").setTabCompleter(new CommandManager(this));
        getCommand("shopadd").setTabCompleter(new CommandManager(this));

        // 7. Events
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryDragListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // 8. Tasks
        long ticksPerHour = 20L * 60L * 60L;
        new ExpiredListingTask(this).runTaskTimerAsynchronously(this, ticksPerHour, ticksPerHour);

        getLogger().info("AeternumShop enabled successfully.");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("AeternumShop disabled.");
    }

    public void reload() {
        configManager.reloadAll();
        currencyManager.loadFromConfig(configManager.getCurrency());
    }

    public static AeternumShop getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
