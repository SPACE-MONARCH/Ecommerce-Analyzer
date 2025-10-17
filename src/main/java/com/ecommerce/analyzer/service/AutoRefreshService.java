package com.ecommerce.analyzer.service;

import com.ecommerce.analyzer.util.ConfigManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Auto Refresh Service
 * Automatically refreshes product data in background
 */
public class AutoRefreshService {

    private final ScheduledExecutorService scheduler;
    private final ProductService productService;
    private final int refreshIntervalMinutes;
    private boolean isRunning;

    public AutoRefreshService() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.productService = new ProductService();
        this.refreshIntervalMinutes = ConfigManager.getAutoRefreshInterval();
        this.isRunning = false;
    }

    /**
     * Start auto-refresh
     */
    public void start() {
        if (isRunning) {
            System.out.println("Auto-refresh is already running");
            return;
        }

        System.out.println("Starting auto-refresh service (interval: " + refreshIntervalMinutes + " minutes)");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Auto-refresh triggered at: " + new java.util.Date());
                refreshAllProducts();
            } catch (Exception e) {
                System.err.println("Auto-refresh error: " + e.getMessage());
            }
        }, refreshIntervalMinutes, refreshIntervalMinutes, TimeUnit.MINUTES);

        isRunning = true;
    }

    /**
     * Stop auto-refresh
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        System.out.println("Stopping auto-refresh service");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        isRunning = false;
    }

    /**
     * Refresh all products in database
     */
    private void refreshAllProducts() {
        try {
            // Get all products and refresh their data
            var products = productService.searchProducts("");

            for (var product : products) {
                try {
                    productService.refreshProduct(product.getId());
                    System.out.println("Refreshed: " + product.getName());

                    // Wait a bit between requests to avoid being blocked
                    Thread.sleep(2000);

                } catch (Exception e) {
                    System.err.println("Failed to refresh product " + product.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("Auto-refresh completed for " + products.size() + " products");

        } catch (Exception e) {
            System.err.println("Auto-refresh failed: " + e.getMessage());
        }
    }

    /**
     * Manual refresh trigger
     */
    public void triggerRefresh() {
        System.out.println("Manual refresh triggered");
        new Thread(this::refreshAllProducts).start();
    }

    /**
     * Check if service is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get refresh interval
     */
    public int getRefreshIntervalMinutes() {
        return refreshIntervalMinutes;
    }
}
