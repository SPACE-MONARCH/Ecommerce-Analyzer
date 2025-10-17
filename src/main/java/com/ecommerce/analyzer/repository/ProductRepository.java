package com.ecommerce.analyzer.repository;

import com.ecommerce.analyzer.model.Product;
import com.ecommerce.analyzer.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Repository
 * JDBC implementation for product database operations
 */
public class ProductRepository {

    /**
     * Insert a new product
     */
    public Long insert(Product product) throws SQLException {
        String sql = "INSERT INTO product (name, product_url) VALUES (?, ?) RETURNING id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getProductUrl());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
            throw new SQLException("Failed to insert product");
        }
    }

    /**
     * Find product by ID
     */
    public Product findById(Long id) throws SQLException {
        String sql = "SELECT * FROM product WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
            return null;
        }
    }

    /**
     * Find product by URL
     */
    public Product findByUrl(String url) throws SQLException {
        String sql = "SELECT * FROM product WHERE product_url = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, url);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
            return null;
        }
    }

    /**
     * Search products by name (for autocomplete)
     */
    public List<Product> searchByName(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM product WHERE LOWER(name) LIKE LOWER(?) LIMIT 10";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    /**
     * Get all products
     */
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT * FROM product ORDER BY last_updated DESC LIMIT 100";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    /**
     * Update product last_updated timestamp
     */
    public void updateTimestamp(Long productId) throws SQLException {
        String sql = "UPDATE product SET last_updated = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, productId);
            stmt.executeUpdate();
        }
    }

    /**
     * Map ResultSet to Product object
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Timestamp lastUpdatedTs = rs.getTimestamp("last_updated");

        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("product_url"),
                createdAtTs != null ? createdAtTs.toLocalDateTime() : null,
                lastUpdatedTs != null ? lastUpdatedTs.toLocalDateTime() : null
        );
    }
}