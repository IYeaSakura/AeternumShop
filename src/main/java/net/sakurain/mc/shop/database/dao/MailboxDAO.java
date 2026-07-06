package net.sakurain.mc.shop.database.dao;

import net.sakurain.mc.shop.database.DatabaseManager;
import net.sakurain.mc.shop.model.MailboxEntry;
import net.sakurain.mc.shop.model.MailboxType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MailboxDAO {

    private final DatabaseManager databaseManager;

    public MailboxDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(MailboxEntry entry) throws SQLException {
        String sql = "INSERT INTO mailbox (player_uuid, entry_type, item_type, item_amount, currency_amount, received, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entry.getPlayerUuid().toString());
            ps.setString(2, entry.getType().name());
            ps.setString(3, entry.getItemType());
            ps.setInt(4, entry.getItemAmount());
            ps.setLong(5, entry.getCurrencyAmount());
            ps.setBoolean(6, entry.isReceived());
            ps.setTimestamp(7, Timestamp.valueOf(entry.getCreateTime()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entry.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<MailboxEntry> findByPlayer(UUID player) throws SQLException {
        List<MailboxEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM mailbox WHERE player_uuid = ? AND received = FALSE ORDER BY create_time ASC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSet(rs));
                }
            }
        }
        return entries;
    }

    public int countByPlayer(UUID player) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mailbox WHERE player_uuid = ? AND received = FALSE";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void markReceived(int id) throws SQLException {
        String sql = "UPDATE mailbox SET received = TRUE WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM mailbox WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private MailboxEntry mapResultSet(ResultSet rs) throws SQLException {
        MailboxEntry entry = new MailboxEntry();
        entry.setId(rs.getInt("id"));
        entry.setPlayerUuid(UUID.fromString(rs.getString("player_uuid")));
        entry.setType(MailboxType.valueOf(rs.getString("entry_type")));
        entry.setItemType(rs.getString("item_type"));
        entry.setItemAmount(rs.getInt("item_amount"));
        entry.setCurrencyAmount(rs.getLong("currency_amount"));
        entry.setReceived(rs.getBoolean("received"));
        Timestamp ts = rs.getTimestamp("create_time");
        entry.setCreateTime(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return entry;
    }
}
