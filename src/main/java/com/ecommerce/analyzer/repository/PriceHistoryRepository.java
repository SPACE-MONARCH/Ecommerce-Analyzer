package com.ecommerce.analyzer.repository;

import com.ecommerce.analyzer.model.PriceHistory;
import com.ecommerce.analyzer.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Price History Repository
 * Manages historical price data for trend analysis
 */
public class PriceHistoryRepository {

    /** Insert price history record */
    public void insert(PriceHistory priceHistory) throws SQLException {
        String sql = "INSERT INTO price_history (product_detail_id, price) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, priceHistory.getProductDetailId());
            stmt.setDouble(2, priceHistory.getPrice());
            stmt.executeUpdate();
        }
    }

    /**
     * Get price history for last 30 days
     */
    public List<PriceHistory> findByProductDetailId(Long productDetailId, int days) throws SQLException {
        String sql = "SELECT * FROM price_history WHERE product_detail_id = ? AND recorded_at >= CURRENT_TIMESTAMP - INTERVAL '" + days + " days' " +
                "ORDER BY recorded_at ASC";

        List<PriceHistory> history = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, productDetailId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                history.add(mapResultSetToPriceHistory(rs));
            }
        }
        return history;
    }

    /**
     * Get all price history for a product detail
     */
    public List<PriceHistory> findAll(Long productDetailId) throws SQLException {
        return findByProductDetailId(productDetailId, 365); // Last year
    }

    /**
     * Delete old price history (older than specified days)
     */
    public void deleteOldRecords(int daysToKeep) throws SQLException {
        String sql = "DELETE FROM price_history " +
                "WHERE recorded_at < CURRENT_TIMESTAMP - INTERVAL '" + daysToKeep + " days'";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            int deleted = stmt.executeUpdate(sql);
            System.out.println("Deleted " + deleted + " old price history records");
        }
    }

    /**
     * Map ResultSet to PriceHistory object
     */
    private PriceHistory mapResultSetToPriceHistory(ResultSet rs) throws SQLException {
        Timestamp recordedAtTs = rs.getTimestamp("recorded_at");

        return new PriceHistory(
                rs.getLong("id"),
                rs.getLong("product_detail_id"),
                rs.getDouble("price"),
                recordedAtTs != null ? recordedAtTs.toLocalDateTime() : null
        );
    }
}