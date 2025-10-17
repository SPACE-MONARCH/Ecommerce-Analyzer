package com.ecommerce.analyzer.service;

import com.ecommerce.analyzer.model.ProductDetail;

import java.util.Random;

public class MockScraperService {

    private Random random = new Random();

    /**
     * Creates realistic mock product data for demos/testing
     */
    public ProductDetail createMockProduct(String productName, String platform, Long productId) {
        System.out.println("[Mock] Creating mock data for " + platform);

        ProductDetail detail = new ProductDetail();
        detail.setProductId(productId);
        detail.setPlatform(platform);
        detail.setProductLink("https://www." + platform.toLowerCase() + ".com/search?q=" + productName);

        // Generate realistic prices based on platform
        double basePrice = getBasePriceByCategory(productName);
        detail.setPrice(adjustPriceByPlatform(basePrice, platform));

        // Generate ratings between 3.5 and 5.0
        detail.setRating(3.5 + random.nextDouble() * 1.5);

        // Random review count
        detail.setReviewCount(random.nextInt(5000) + 100);

        // Platform-specific sellers
        detail.setSeller(getPlatformSeller(platform));

        // Delivery estimates
        detail.setDeliveryTime((2 + random.nextInt(5)) + " days delivery");

        // Return policies
        detail.setReturnPolicy(getReturnPolicy(platform));

        detail.setWarranty("1 year manufacturer warranty");
        detail.setOffers(getRandomOffer());
        detail.setAvailability(random.nextBoolean() ? "In Stock" : "Limited Stock");

        return detail;
    }

    private double getBasePriceByCategory(String productName) {
        String lower = productName.toLowerCase();
        if (lower.contains("laptop") || lower.contains("macbook")) {
            return 45000 + random.nextDouble() * 55000; // 45k-100k
        } else if (lower.contains("phone") || lower.contains("iphone") || lower.contains("samsung")) {
            return 15000 + random.nextDouble() * 85000; // 15k-100k
        } else if (lower.contains("watch") || lower.contains("smartwatch")) {
            return 2000 + random.nextDouble() * 48000; // 2k-50k
        } else if (lower.contains("shoe") || lower.contains("sneaker")) {
            return 1500 + random.nextDouble() * 8500; // 1.5k-10k
        } else if (lower.contains("shirt") || lower.contains("tshirt")) {
            return 300 + random.nextDouble() * 1700; // 300-2k
        }
        return 500 + random.nextDouble() * 9500; // Default: 500-10k
    }

    private double adjustPriceByPlatform(double basePrice, String platform) {
        // Simulate price differences across platforms
        switch (platform.toLowerCase()) {
            case "amazon":
                return basePrice * (0.95 + random.nextDouble() * 0.1); // -5% to +5%
            case "flipkart":
                return basePrice * (0.92 + random.nextDouble() * 0.15); // -8% to +7%
            case "myntra":
                return basePrice * (0.98 + random.nextDouble() * 0.08); // -2% to +6%
            case "meesho":
                return basePrice * (0.85 + random.nextDouble() * 0.1); // -15% to -5%
            case "ajio":
                return basePrice * (0.93 + random.nextDouble() * 0.12); // -7% to +5%
            case "snapdeal":
                return basePrice * (0.90 + random.nextDouble() * 0.15); // -10% to +5%
            default:
                return basePrice;
        }
    }

    private String getPlatformSeller(String platform) {
        switch (platform.toLowerCase()) {
            case "amazon": return "Amazon Retail";
            case "flipkart": return "Flipkart Assured";
            case "myntra": return "Myntra Official";
            case "meesho": return "Meesho Store";
            case "ajio": return "AJIO Fashion";
            case "snapdeal": return "Snapdeal Direct";
            default: return platform + " Official";
        }
    }

    private String getReturnPolicy(String platform) {
        switch (platform.toLowerCase()) {
            case "amazon": return "30 days return & refund";
            case "flipkart": return "10 days return policy";
            case "myntra": return "30 days easy return";
            case "meesho": return "7 days return available";
            case "ajio": return "15 days return policy";
            case "snapdeal": return "10 days return policy";
            default: return "Check website for returns";
        }
    }

    private String getRandomOffer() {
        String[] offers = {
                "Get 10% instant discount",
                "15% off with bank offer",
                "Buy 2 Get 1 Free",
                "Flat 20% off on prepaid orders",
                "Extra 5% off with app",
                "No Cost EMI available",
                "Exchange offer available"
        };
        return offers[random.nextInt(offers.length)];
    }
}
