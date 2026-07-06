package net.sakurain.mc.shop.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.sakurain.mc.shop.AeternumShop;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteDatabase {

    private final AeternumShop plugin;
    private HikariDataSource dataSource;

    public SQLiteDatabase(AeternumShop plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        String url = "jdbc:sqlite:" + new File(dataFolder, "data.db").getAbsolutePath();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(0);
        hikariConfig.setMaxLifetime(0);

        // SQLite specific optimizations
        hikariConfig.addDataSourceProperty("journal_mode", "WAL");
        hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
        hikariConfig.addDataSourceProperty("busy_timeout", "30000");

        dataSource = new HikariDataSource(hikariConfig);
        plugin.getLogger().info("SQLite connected.");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
