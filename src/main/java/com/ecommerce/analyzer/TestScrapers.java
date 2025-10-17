package com.ecommerce.analyzer;

import com.ecommerce.analyzer.model.ProductDetail;
import com.ecommerce.analyzer.service.ScraperService;

public class TestScrapers {

    public static void main(String[] args) {
        ScraperService scraper = new ScraperService();

        String productName = "iPhone 15";
        String flipkartUrl = "https://www.flipkart.com/search?q=iphone+15";
        String amazonUrl = "https://www.amazon.in/s?k=iphone+15";

        System.out.println("=====================================");
        System.out.println("   SCRAPER TESTING SIMULATION");
        System.out.println("=====================================\n");

        // Test 1: HtmlUnit Only
        System.out.println("\n>>> TEST 1: HtmlUnit Only <<<");
        scraper.setUseHtmlUnit(true);
        scraper.setUseSelenium(false);
        scraper.setUseMockFallback(false);

        ProductDetail result1 = scraper.scrapeProduct(flipkartUrl, productName, 1L);
        printResult("HtmlUnit", result1);

        // Test 2: Selenium Only
        System.out.println("\n>>> TEST 2: Selenium Only <<<");
        scraper.setUseHtmlUnit(false);
        scraper.setUseSelenium(true);
        scraper.setUseMockFallback(false);

        ProductDetail result2 = scraper.scrapeProduct(flipkartUrl, productName, 2L);
        printResult("Selenium", result2);

        // Test 3: Hybrid Approach (Recommended)
        System.out.println("\n>>> TEST 3: Hybrid (HtmlUnit -> Selenium -> Mock) <<<");
        scraper.setUseHtmlUnit(true);
        scraper.setUseSelenium(true);
        scraper.setUseMockFallback(true);

        ProductDetail result3 = scraper.scrapeProduct(amazonUrl, productName, 3L);
        printResult("Hybrid", result3);

        System.out.println("\n=====================================");
        System.out.println("   TESTING COMPLETE");
        System.out.println("=====================================");
    }

    private static void printResult(String testName, ProductDetail detail) {
        System.out.println("\n--- " + testName + " Result ---");
        if (detail == null) {
            System.out.println("X Scraping failed - No data returned"); // Changed from ❌
            return;
        }

        System.out.println("✓ Scraping successful!");
        System.out.println("  Product ID:    " + detail.getProductId());
        System.out.println("  Platform:      " + detail.getPlatform());
        System.out.println("  Price:         Rs " + String.format("%.2f", detail.getPrice()));
        System.out.println("  Rating:        " + detail.getRating());
        System.out.println("  Reviews:       " + detail.getReviewCount());
        System.out.println("  Availability:  " + detail.getAvailability());
        System.out.println("  Seller:        " + detail.getSeller());
        System.out.println("  Delivery:      " + detail.getDeliveryTime());
        System.out.println("  Link:          " + detail.getProductLink());
    }

}
