package com.ecommerce.analyzer.repository;

import com.ecommerce.analyzer.model.ProductDetail;
import com.ecommerce.analyzer.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailRepository {
    /** Find all product details by product ID */
    public List<ProductDetail> findByProductId(Long productId) throws SQLException {
        String sql = """
            SELECT 
                id, 
                product_id, 
                platform, 
                price, 
                rating, 
                seller, 
                delivery_time, 
                return_policy, 
                warranty, 
                offers, 
                product_link, 
                reviewcount, 
                availability,
                last_scraped
            FROM product_detail 
            WHERE product_id = ?
            ORDER BY price ASC
            """;

        List<ProductDetail> details = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                details.add(mapResultSetToProductDetail(rs));
            }
        }

        return details;
    }

    /** Upsert product detail */
    public Long upsert(ProductDetail detail) throws SQLException {
        // First, try to find existing record
        String checkSql = "SELECT id FROM product_detail WHERE product_id = ? AND platform = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setLong(1, detail.getProductId());
            checkStmt.setString(2, detail.getPlatform());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Record exists - UPDATE
                Long existingId = rs.getLong("id");
                String updateSql = """
                UPDATE product_detail SET
                    price = ?,
                    rating = ?,
                    seller = ?,
                    delivery_time = ?,
                    return_policy = ?,
                    warranty = ?,
                    offers = ?,
                    product_link = ?,
                    reviewcount = ?,
                    availability = ?,
                    last_scraped = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, detail.getPrice());
                    updateStmt.setDouble(2, detail.getRating());
                    updateStmt.setString(3, detail.getSeller());
                    updateStmt.setString(4, detail.getDeliveryTime());
                    updateStmt.setString(5, detail.getReturnPolicy());
                    updateStmt.setString(6, detail.getWarranty());
                    updateStmt.setString(7, detail.getOffers());
                    updateStmt.setString(8, detail.getProductLink());
                    updateStmt.setInt(9, detail.getReviewCount());
                    updateStmt.setString(10, detail.getAvailability());
                    updateStmt.setLong(11, existingId);

                    updateStmt.executeUpdate();
                    return existingId;
                }
            } else {
                // Record doesn't exist - INSERT
                String insertSql = """
                INSERT INTO product_detail (
                    product_id, platform, price, rating, seller, 
                    delivery_time, return_policy, warranty, offers, 
                    product_link, reviewcount, availability
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setLong(1, detail.getProductId());
                    insertStmt.setString(2, detail.getPlatform());
                    insertStmt.setDouble(3, detail.getPrice());
                    insertStmt.setDouble(4, detail.getRating());
                    insertStmt.setString(5, detail.getSeller());
                    insertStmt.setString(6, detail.getDeliveryTime());
                    insertStmt.setString(7, detail.getReturnPolicy());
                    insertStmt.setString(8, detail.getWarranty());
                    insertStmt.setString(9, detail.getOffers());
                    insertStmt.setString(10, detail.getProductLink());
                    insertStmt.setInt(11, detail.getReviewCount());
                    insertStmt.setString(12, detail.getAvailability());

                    ResultSet insertRs = insertStmt.executeQuery();
                    if (insertRs.next()) {
                        return insertRs.getLong("id");
                    }
                }
            }
        }
        throw new SQLException("Failed to upsert product detail");
    }

    /**
     * Delete product detail
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM product_detail WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Map ResultSet to ProductDetail - FIXED: column names with underscores
     */
    private ProductDetail mapResultSetToProductDetail(ResultSet rs) throws SQLException {
        ProductDetail detail = new ProductDetail();

        detail.setId(rs.getLong("id"));
        detail.setProductId(rs.getLong("product_id"));
        detail.setPlatform(rs.getString("platform"));
        detail.setPrice(rs.getDouble("price"));
        detail.setRating(rs.getDouble("rating"));
        detail.setSeller(rs.getString("seller"));
        detail.setDeliveryTime(rs.getString("delivery_time"));
        detail.setReturnPolicy(rs.getString("return_policy"));
        detail.setWarranty(rs.getString("warranty"));
        detail.setOffers(rs.getString("offers"));
        detail.setProductLink(rs.getString("product_link"));
        detail.setReviewCount(rs.getInt("reviewcount"));
        detail.setAvailability(rs.getString("availability"));

        Timestamp lastScraped = rs.getTimestamp("last_scraped");
        if (lastScraped != null) {
            detail.setLastScraped(lastScraped.toLocalDateTime());
        }

        return detail;
    }
}
