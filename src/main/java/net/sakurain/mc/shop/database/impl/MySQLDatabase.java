package net.sakurain.mc.shop.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.sakurain.mc.shop.AeternumShop;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase {

    private final AeternumShop plugin;
    private HikariDataSource dataSource;

    public MySQLDatabase(AeternumShop plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        YamlConfiguration config = plugin.getConfigManager().getConfig();
        String host = config.getString("database.mysql.host", "localhost");
        int port = config.getInt("database.mysql.port", 3306);
        String database = config.getString("database.mysql.database", "aeternum_shop");
        String username = config.getString("database.mysql.username", "root");
        String password = config.getString("database.mysql.password", "");
        int poolSize = config.getInt("database.mysql.pool-size", 10);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true",
                host, port, database));
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMinimumIdle(Math.max(1, poolSize / 2));
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setLeakDetectionThreshold(60000);

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");

        dataSource = new HikariDataSource(hikariConfig);
        plugin.getLogger().info("MySQL connected.");
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
