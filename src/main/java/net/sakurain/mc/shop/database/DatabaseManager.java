package net.sakurain.mc.shop.database;

import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.database.dao.MailboxDAO;
import net.sakurain.mc.shop.database.dao.PlayerListingDAO;
import net.sakurain.mc.shop.database.dao.TradeRecordDAO;
import net.sakurain.mc.shop.database.impl.MySQLDatabase;
import net.sakurain.mc.shop.database.impl.SQLiteDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private final AeternumShop plugin;
    private DatabaseType type;
    private SQLiteDatabase sqlite;
    private MySQLDatabase mysql;

    private PlayerListingDAO playerListingDAO;
    private TradeRecordDAO tradeRecordDAO;
    private MailboxDAO mailboxDAO;

    public DatabaseManager(AeternumShop plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String typeStr = plugin.getConfigManager().getString("database.type", "SQLITE").toUpperCase();
        try {
            this.type = DatabaseType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid database type '" + typeStr + "', defaulting to SQLITE.");
            this.type = DatabaseType.SQLITE;
        }

        switch (type) {
            case MYSQL -> {
                this.mysql = new MySQLDatabase(plugin);
                this.mysql.connect();
            }
            default -> {
                this.sqlite = new SQLiteDatabase(plugin);
                this.sqlite.connect();
            }
        }

        createTables();

        this.playerListingDAO = new PlayerListingDAO(this);
        this.tradeRecordDAO = new TradeRecordDAO(this);
        this.mailboxDAO = new MailboxDAO(this);
    }

    public Connection getConnection() throws SQLException {
        return switch (type) {
            case MYSQL -> mysql.getConnection();
            default -> sqlite.getConnection();
        };
    }

    public void close() {
        if (sqlite != null) {
            sqlite.disconnect();
        }
        if (mysql != null) {
            mysql.disconnect();
        }
    }

    public DatabaseType getType() {
        return type;
    }

    public PlayerListingDAO getPlayerListingDAO() {
        return playerListingDAO;
    }

    public TradeRecordDAO getTradeRecordDAO() {
        return tradeRecordDAO;
    }

    public MailboxDAO getMailboxDAO() {
        return mailboxDAO;
    }

    private void createTables() {
        String listingsSql = """
                CREATE TABLE IF NOT EXISTS player_listings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    seller VARCHAR(36) NOT NULL,
                    seller_name VARCHAR(16) NOT NULL,
                    item_type VARCHAR(64) NOT NULL,
                    item_amount INTEGER NOT NULL,
                    price INTEGER NOT NULL,
                    post_time TIMESTAMP NOT NULL,
                    status VARCHAR(16) DEFAULT 'ACTIVE'
                );
                """;

        String recordsSql = """
                CREATE TABLE IF NOT EXISTS trade_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trade_type VARCHAR(16) NOT NULL,
                    buyer VARCHAR(36),
                    buyer_name VARCHAR(16),
                    seller VARCHAR(36),
                    seller_name VARCHAR(16),
                    item_type VARCHAR(64) NOT NULL,
                    item_amount INTEGER NOT NULL,
                    price INTEGER NOT NULL,
                    trade_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        String mailboxSql = """
                CREATE TABLE IF NOT EXISTS mailbox (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    entry_type VARCHAR(16) NOT NULL,
                    item_type VARCHAR(64),
                    item_amount INTEGER,
                    currency_amount INTEGER,
                    received BOOLEAN DEFAULT FALSE,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        if (type == DatabaseType.MYSQL) {
            listingsSql = listingsSql.replace("AUTOINCREMENT", "AUTO_INCREMENT");
            recordsSql = recordsSql.replace("AUTOINCREMENT", "AUTO_INCREMENT");
            mailboxSql = mailboxSql.replace("AUTOINCREMENT", "AUTO_INCREMENT");
            listingsSql = listingsSql.replace("BOOLEAN", "TINYINT(1)");
            mailboxSql = mailboxSql.replace("BOOLEAN", "TINYINT(1)");
        }

        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute(listingsSql);
            stmt.execute(recordsSql);
            stmt.execute(mailboxSql);
            plugin.getLogger().info("Database tables created/verified.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
