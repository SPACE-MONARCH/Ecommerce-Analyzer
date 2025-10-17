package com.ecommerce.analyzer.service;

import com.ecommerce.analyzer.model.Product;
import com.ecommerce.analyzer.model.ProductDetail;
import com.ecommerce.analyzer.model.PriceHistory;
import com.ecommerce.analyzer.repository.ProductRepository;
import com.ecommerce.analyzer.repository.ProductDetailRepository;
import com.ecommerce.analyzer.repository.PriceHistoryRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Product Service
 * Business logic for product operations
 */
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ScraperService scraperService;

    public ProductService() {
        this.productRepository = new ProductRepository();
        this.productDetailRepository = new ProductDetailRepository();
        this.priceHistoryRepository = new PriceHistoryRepository();
        this.scraperService = new ScraperService();
    }

    /**
     * Add or update product from URL
     * UPDATED to use multi-platform scraping
     */
    public Product addProductByUrl(String url, String productName) throws SQLException {
        // Check if product already exists
        Product existingProduct = productRepository.findByUrl(url);

        if (existingProduct != null) {
            return existingProduct;
        }

        // Create new product
        Product product = new Product();
        product.setName(productName);
        product.setProductUrl(url);

        Long productId = productRepository.insert(product);
        product.setId(productId);

        // NEW: Scrape ALL platforms at once (instead of single platform)
        scrapeAllPlatformDetails(productId);

        return product;
    }


    /**
     * Search products by name (for autocomplete)
     */
    public List<Product> searchProducts(String searchTerm) throws SQLException {
        return productRepository.searchByName(searchTerm);
    }

    /**
     * Scrape product details for ALL platforms at once
     * This is the preferred method for multi-platform comparison
     */
    public void scrapeAllPlatformDetails(Long productId) throws SQLException {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new SQLException("Product not found with ID: " + productId);
        }

        System.out.println("\n[ProductService] 🚀 Starting multi-platform data retrieval...");
        System.out.println("[ProductService] Product: " + product.getName());

        // Get data for ALL platforms at once using Gemini
        List<ProductDetail> allPlatforms = scraperService.scrapeAllPlatforms(
                product.getName(),
                productId
        );

        if (allPlatforms != null && !allPlatforms.isEmpty()) {
            System.out.println("[ProductService] 💾 Saving " + allPlatforms.size() + " platform details to database...");

            // Save each platform's data
            for (ProductDetail detail : allPlatforms) {
                Long detailId = productDetailRepository.upsert(detail);

                // Record price history for each platform
                PriceHistory priceHistory = new PriceHistory();
                priceHistory.setProductDetailId(detailId);
                priceHistory.setPrice(detail.getPrice());
                priceHistoryRepository.insert(priceHistory);

                System.out.println("  ✓ " + detail.getPlatform() + ": ₹" +
                        String.format("%.2f", detail.getPrice()) +
                        " (" + detail.getRating() + "⭐)");
            }

            System.out.println("[ProductService] ✓ All platform data saved successfully!\n");
        } else {
            System.err.println("[ProductService] ⚠ No product details retrieved");
        }
    }

    /**
     * Get all product details for a product
     */
    public List<ProductDetail> getProductDetails(Long productId) throws SQLException {
        return productDetailRepository.findByProductId(productId);
    }

    /**
     * Get price history for a product detail
     */
    public List<PriceHistory> getPriceHistory(Long productDetailId, int days) throws SQLException {
        return priceHistoryRepository.findByProductDetailId(productDetailId, days);
    }

    /**
     * Refresh product data (re-scrape)
     */
    public void refreshProduct(Long productId) throws SQLException {
        Product product = productRepository.findByUrl(
                productRepository.searchByName("").stream()
                        .filter(p -> p.getId().equals(productId))
                        .findFirst()
                        .orElseThrow(() -> new SQLException("Product not found"))
                        .getProductUrl()
        );
        if (product != null) {
            ProductDetail detail = scraperService.scrapeProduct(
                    product.getProductUrl(),
                    product.getName(),  // ADD THIS PARAMETER
                    productId
            );
            if (detail != null) {
                Long detailId = productDetailRepository.upsert(detail);

                // Update price history
                PriceHistory priceHistory = new PriceHistory();
                priceHistory.setProductDetailId(detailId);
                priceHistory.setPrice(detail.getPrice());
                priceHistoryRepository.insert(priceHistory);
            }
            productRepository.updateTimestamp(productId);
        }
    }
}