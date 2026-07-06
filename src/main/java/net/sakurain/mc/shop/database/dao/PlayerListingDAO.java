package net.sakurain.mc.shop.database.dao;

import net.sakurain.mc.shop.database.DatabaseManager;
import net.sakurain.mc.shop.model.ListingStatus;
import net.sakurain.mc.shop.model.PlayerListing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerListingDAO {

    private final DatabaseManager databaseManager;

    public PlayerListingDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(PlayerListing listing) throws SQLException {
        String sql = "INSERT INTO player_listings (seller, seller_name, item_type, item_amount, price, post_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, listing.getSeller().toString());
            ps.setString(2, listing.getSellerName());
            ps.setString(3, listing.getItemType());
            ps.setInt(4, listing.getItemAmount());
            ps.setLong(5, listing.getPrice());
            ps.setTimestamp(6, Timestamp.valueOf(listing.getPostTime()));
            ps.setString(7, listing.getStatus().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    listing.setId(rs.getInt(1));
                }
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM player_listings WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int id, ListingStatus status) throws SQLException {
        String sql = "UPDATE player_listings SET status = ? WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<PlayerListing> findAllActive() throws SQLException {
        List<PlayerListing> listings = new ArrayList<>();
        String sql = "SELECT * FROM player_listings WHERE status = 'ACTIVE' ORDER BY post_time DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                listings.add(mapResultSet(rs));
            }
        }
        return listings;
    }

    public List<PlayerListing> findBySeller(UUID seller) throws SQLException {
        List<PlayerListing> listings = new ArrayList<>();
        String sql = "SELECT * FROM player_listings WHERE seller = ? ORDER BY post_time DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, seller.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listings.add(mapResultSet(rs));
                }
            }
        }
        return listings;
    }

    public int countActiveBySeller(UUID seller) throws SQLException {
        String sql = "SELECT COUNT(*) FROM player_listings WHERE seller = ? AND status = 'ACTIVE'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, seller.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public Optional<PlayerListing> findById(int id) throws SQLException {
        String sql = "SELECT * FROM player_listings WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<PlayerListing> findExpired(LocalDateTime now) throws SQLException {
        List<PlayerListing> listings = new ArrayList<>();
        String sql = "SELECT * FROM player_listings WHERE status = 'ACTIVE' AND post_time <= ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(now));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listings.add(mapResultSet(rs));
                }
            }
        }
        return listings;
    }

    private PlayerListing mapResultSet(ResultSet rs) throws SQLException {
        PlayerListing listing = new PlayerListing();
        listing.setId(rs.getInt("id"));
        listing.setSeller(UUID.fromString(rs.getString("seller")));
        listing.setSellerName(rs.getString("seller_name"));
        listing.setItemType(rs.getString("item_type"));
        listing.setItemAmount(rs.getInt("item_amount"));
        listing.setPrice(rs.getLong("price"));
        Timestamp ts = rs.getTimestamp("post_time");
        listing.setPostTime(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        listing.setStatus(ListingStatus.valueOf(rs.getString("status")));
        return listing;
    }
}
