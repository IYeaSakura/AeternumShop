package net.sakurain.mc.shop.database.dao;

import net.sakurain.mc.shop.database.DatabaseManager;
import net.sakurain.mc.shop.model.TradeRecord;
import net.sakurain.mc.shop.model.TradeType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeRecordDAO {

    private final DatabaseManager databaseManager;

    public TradeRecordDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(TradeRecord record) throws SQLException {
        String sql = "INSERT INTO trade_records (trade_type, buyer, buyer_name, seller, seller_name, item_type, item_amount, price, trade_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getTradeType().name());
            ps.setString(2, record.getBuyer() != null ? record.getBuyer().toString() : null);
            ps.setString(3, record.getBuyerName());
            ps.setString(4, record.getSeller() != null ? record.getSeller().toString() : null);
            ps.setString(5, record.getSellerName());
            ps.setString(6, record.getItemType());
            ps.setInt(7, record.getItemAmount());
            ps.setLong(8, record.getPrice());
            ps.setTimestamp(9, Timestamp.valueOf(record.getTradeTime()));
            ps.executeUpdate();
        }
    }

    public List<TradeRecord> findRecentByPlayer(UUID player, int limit) throws SQLException {
        List<TradeRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM trade_records WHERE buyer = ? OR seller = ? ORDER BY trade_time DESC LIMIT ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, player.toString());
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSet(rs));
                }
            }
        }
        return records;
    }

    public List<TradeRecord> findRecentGlobal(int limit) throws SQLException {
        List<TradeRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM trade_records ORDER BY trade_time DESC LIMIT ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSet(rs));
                }
            }
        }
        return records;
    }

    private TradeRecord mapResultSet(ResultSet rs) throws SQLException {
        TradeRecord record = new TradeRecord();
        record.setId(rs.getInt("id"));
        record.setTradeType(TradeType.valueOf(rs.getString("trade_type")));
        String buyer = rs.getString("buyer");
        record.setBuyer(buyer != null ? UUID.fromString(buyer) : null);
        record.setBuyerName(rs.getString("buyer_name"));
        String seller = rs.getString("seller");
        record.setSeller(seller != null ? UUID.fromString(seller) : null);
        record.setSellerName(rs.getString("seller_name"));
        record.setItemType(rs.getString("item_type"));
        record.setItemAmount(rs.getInt("item_amount"));
        record.setPrice(rs.getLong("price"));
        Timestamp ts = rs.getTimestamp("trade_time");
        record.setTradeTime(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return record;
    }
}
