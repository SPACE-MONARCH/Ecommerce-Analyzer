package com.ecommerce.analyzer.service;
import com.ecommerce.analyzer.model.ProductDetail;
import java.util.List;
import java.util.ArrayList;

public class ScraperService {
    private HtmlUnitScraperService htmlUnitScraper = new HtmlUnitScraperService();
    private SeleniumScraperService seleniumScraper = new SeleniumScraperService();
    private MockScraperService mockScraper = new MockScraperService();
    private GeminiProductScraperService geminiScraper = new GeminiProductScraperService(); // NEW

    // Configuration flags for testing
    private boolean useGemini = true;          // NEW - Try Gemini first
    private boolean useHtmlUnit = false;       // Keep as backup
    private boolean useSelenium = false;       // Keep as backup
    private boolean useMockFallback = true;    // Always have fallback

    /**
     * NEW METHOD: Gets data for ALL platforms at once
     * This is now the primary method for multi-platform comparison
     */
    public List<ProductDetail> scrapeAllPlatforms(String productName, Long productId) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║       MULTI-PLATFORM PRODUCT DATA RETRIEVAL          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("Product: " + productName);
        System.out.println("Platforms: Amazon, Flipkart, Myntra, Meesho, Ajio, Snapdeal, Nykaa, Tata CLiQ, FirstCry, Shopify\n");

        List<ProductDetail> results = null;

        // Strategy 1: Try Gemini AI (RECOMMENDED - gets all platforms at once)
        if (useGemini) {
            System.out.println("[Strategy 1] 🤖 Attempting Gemini AI data generation...");
            results = geminiScraper.scrapeAllPlatforms(productName, productId);
            if (results != null && !results.isEmpty()) {
                System.out.println("✓ Gemini successful! Retrieved " + results.size() + " platforms\n");
                return results;
            }
            System.out.println("✗ Gemini failed or returned empty data\n");
        }

        // Strategy 2: Try traditional scraping for major platforms
        if (useHtmlUnit || useSelenium) {
            System.out.println("[Strategy 2] 🌐 Attempting traditional web scraping...");
            results = scrapeTraditionalPlatforms(productName, productId);
            if (results != null && !results.isEmpty()) {
                System.out.println("✓ Traditional scraping successful! Retrieved " + results.size() + " platforms\n");
                return results;
            }
            System.out.println("✗ Traditional scraping failed\n");
        }

        // Strategy 3: Fallback to mock data (always works)
        if (useMockFallback) {
            System.out.println("[Strategy 3] 📦 Using enhanced mock data as fallback...");
            results = generateMockForAllPlatforms(productName, productId);
            System.out.println("✓ Mock data generated for " + results.size() + " platforms\n");
            return results;
        }

        System.out.println("✗ All scraping strategies failed\n");
        return new ArrayList<>();
    }

    /**
     * LEGACY METHOD: Single platform scraping (for backward compatibility)
     * Now internally calls scrapeAllPlatforms and filters by URL
     */
    public ProductDetail scrapeProduct(String url, String productName, Long productId) {
        String platform = determinePlatform(url);

        System.out.println("\n========================================");
        System.out.println("Starting scrape for: " + productName);
        System.out.println("Platform: " + platform);
        System.out.println("========================================\n");

        // If specific platform requested, try to scrape just that one
        ProductDetail result = null;

        // Strategy 1: Try HtmlUnit first (faster, 50-60% success)
        if (useHtmlUnit) {
            System.out.println("[Strategy 1] Attempting HtmlUnit scraping...");
            result = scrapeWithHtmlUnit(platform, productName, productId);
            if (result != null && result.getPrice() > 0) {
                System.out.println("✓ HtmlUnit scraping successful!\n");
                return result;
            }
            System.out.println("✗ HtmlUnit failed or returned incomplete data\n");
        }

        // Strategy 2: Try Selenium (slower but higher success 85-95%)
        if (useSelenium) {
            System.out.println("[Strategy 2] Attempting Selenium scraping...");
            result = scrapeWithSelenium(platform, productName, productId);
            if (result != null && result.getPrice() > 0) {
                System.out.println("✓ Selenium scraping successful!\n");
                return result;
            }
            System.out.println("✗ Selenium failed or returned incomplete data\n");
        }

        // Strategy 3: Try Gemini for this specific platform
        if (useGemini) {
            System.out.println("[Strategy 3] Attempting Gemini AI for " + platform + "...");
            List<ProductDetail> geminiResults = geminiScraper.scrapeAllPlatforms(productName, productId);
            if (geminiResults != null) {
                // Find the specific platform
                for (ProductDetail detail : geminiResults) {
                    if (detail.getPlatform().equalsIgnoreCase(platform)) {
                        System.out.println("✓ Gemini found data for " + platform + "\n");
                        return detail;
                    }
                }
            }
        }

        // Strategy 4: Fallback to mock data (always works)
        if (useMockFallback) {
            System.out.println("[Strategy 4] Using mock data as fallback...");
            result = mockScraper.createMockProduct(productName, platform, productId);
            System.out.println("✓ Mock data generated\n");
            return result;
        }

        System.out.println("✗ All scraping strategies failed\n");
        return null;
    }

    /**
     * NEW: Scrapes traditional platforms (Amazon, Flipkart) using HtmlUnit/Selenium
     */
    private List<ProductDetail> scrapeTraditionalPlatforms(String productName, Long productId) {
        List<ProductDetail> results = new ArrayList<>();
        String[] platforms = {"Flipkart", "Amazon"};

        for (String platform : platforms) {
            ProductDetail detail = null;

            if (useHtmlUnit) {
                detail = scrapeWithHtmlUnit(platform, productName, productId);
            }

            if (detail == null && useSelenium) {
                detail = scrapeWithSelenium(platform, productName, productId);
            }

            if (detail != null && detail.getPrice() > 0) {
                results.add(detail);
            }
        }

        return results.isEmpty() ? null : results;
    }

    /**
     * NEW: Generates mock data for all platforms
     */
    private List<ProductDetail> generateMockForAllPlatforms(String productName, Long productId) {
        List<ProductDetail> results = new ArrayList<>();
        String[] platforms = {"Amazon", "Flipkart", "Myntra", "Meesho", "Ajio", "Snapdeal", "Nykaa", "Tata CLiQ"};

        for (String platform : platforms) {
            results.add(mockScraper.createMockProduct(productName, platform, productId));
        }

        return results;
    }

    /**
     * Scrapes using HtmlUnit based on platform
     */
    private ProductDetail scrapeWithHtmlUnit(String platform, String productName, Long productId) {
        try {
            switch (platform.toLowerCase()) {
                case "flipkart":
                    return htmlUnitScraper.scrapeFlipkart(productName, productId);
                case "amazon":
                    return htmlUnitScraper.scrapeAmazon(productName, productId);
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("HtmlUnit exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Scrapes using Selenium based on platform
     */
    private ProductDetail scrapeWithSelenium(String platform, String productName, Long productId) {
        try {
            switch (platform.toLowerCase()) {
                case "flipkart":
                    return seleniumScraper.scrapeFlipkart(productName, productId);
                case "amazon":
                    return seleniumScraper.scrapeAmazon(productName, productId);
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Selenium exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Determines e-commerce platform from URL
     */
    private String determinePlatform(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("amazon")) return "Amazon";
        if (lowerUrl.contains("flipkart")) return "Flipkart";
        if (lowerUrl.contains("myntra")) return "Myntra";
        if (lowerUrl.contains("meesho")) return "Meesho";
        if (lowerUrl.contains("ajio")) return "Ajio";
        if (lowerUrl.contains("snapdeal")) return "Snapdeal";
        if (lowerUrl.contains("nykaa")) return "Nykaa";
        if (lowerUrl.contains("tatacliq") || lowerUrl.contains("tata")) return "Tata CLiQ";
        if (lowerUrl.contains("firstcry")) return "FirstCry";
        if (lowerUrl.contains("shopify")) return "Shopify";
        return "Unknown";
    }

    // Configuration methods for testing different strategies
    public void setUseGemini(boolean use) {
        this.useGemini = use;
    }
    public void setUseHtmlUnit(boolean use) {
        this.useHtmlUnit = use;
    }
    public void setUseSelenium(boolean use) {
        this.useSelenium = use;
    }
    public void setUseMockFallback(boolean use) {
        this.useMockFallback = use;
    }
}
