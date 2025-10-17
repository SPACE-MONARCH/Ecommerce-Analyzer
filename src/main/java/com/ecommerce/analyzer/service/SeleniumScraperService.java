package com.ecommerce.analyzer.service;

import com.ecommerce.analyzer.model.ProductDetail;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class SeleniumScraperService {

    private static boolean isDriverSetup = false;

    /**
     * Sets up ChromeDriver automatically using WebDriverManager
     * This only needs to run once per application session
     */
    private void setupChromeDriver() {
        if (!isDriverSetup) {
            System.out.println("[Selenium] Setting up ChromeDriver...");
            WebDriverManager.chromedriver().setup();
            isDriverSetup = true;
            System.out.println("[Selenium] ChromeDriver ready");
        }
    }

    /**
     * Scrapes Flipkart using Selenium (highest success rate)
     */
    public ProductDetail scrapeFlipkart(String productName, Long productId) {
        System.out.println("[Selenium] Scraping Flipkart for: " + productName);
        setupChromeDriver();

        WebDriver driver = null;
        try {
            driver = createChromeDriver();
            Thread.sleep(2000); // Rate limiting

            String encodedSearch = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String url = "https://www.flipkart.com/search?q=" + encodedSearch;

            driver.get(url);

            // Wait for products to load (max 10 seconds)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-id]")));

            // Find first product
            List<WebElement> products = driver.findElements(By.cssSelector("div[data-id]"));
            if (products.isEmpty()) {
                products = driver.findElements(By.cssSelector("div._1AtVbE"));
            }

            if (products.isEmpty()) {
                System.out.println("[Selenium] No products found for Flipkart");
                return null;
            }

            WebElement firstProduct = products.get(0);

            ProductDetail detail = new ProductDetail();
            detail.setProductId(productId);
            detail.setPlatform("Flipkart");
            detail.setProductLink(url);

            // Extract data with multiple selector fallbacks
            detail.setPrice(extractPrice(firstProduct, "div._30jeq3", "div._3I9_wc"));
            detail.setRating(extractRating(firstProduct, "div._3LWZlK"));
            detail.setSeller(extractText(firstProduct, "div._2WkVRV"));
            detail.setDeliveryTime("Check website");
            detail.setReturnPolicy("10 days return");
            detail.setAvailability("In Stock");

            System.out.println("[Selenium] ✓ Flipkart scraping successful");
            return detail;

        } catch (Exception e) {
            System.err.println("[Selenium] Flipkart scraping failed: " + e.getMessage());
            return null;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * Scrapes Amazon using Selenium
     */
    public ProductDetail scrapeAmazon(String productName, Long productId) {
        System.out.println("[Selenium] Scraping Amazon for: " + productName);
        setupChromeDriver();

        WebDriver driver = null;
        try {
            driver = createChromeDriver();
            Thread.sleep(3000); // Amazon needs longer delay

            String encodedSearch = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String url = "https://www.amazon.in/s?k=" + encodedSearch;

            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-component-type='s-search-result']")
            ));

            List<WebElement> products = driver.findElements(
                    By.cssSelector("div[data-component-type='s-search-result']")
            );

            if (products.isEmpty()) {
                System.out.println("[Selenium] No products found for Amazon");
                return null;
            }

            WebElement firstProduct = products.get(0);

            ProductDetail detail = new ProductDetail();
            detail.setProductId(productId);
            detail.setPlatform("Amazon");
            detail.setProductLink(url);

            detail.setPrice(extractPrice(firstProduct, "span.a-price-whole"));
            detail.setRating(extractRating(firstProduct, "span.a-icon-alt"));
            detail.setSeller("Amazon");
            detail.setDeliveryTime("2-3 days");
            detail.setReturnPolicy("30 days return");
            detail.setAvailability("In Stock");

            System.out.println("[Selenium] ✓ Amazon scraping successful");
            return detail;

        } catch (Exception e) {
            System.err.println("[Selenium] Amazon scraping failed: " + e.getMessage());
            return null;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    /**
     * Creates configured ChromeDriver with anti-detection settings
     */
    private WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();

        // Headless mode (no GUI) - faster and less resource intensive
        options.addArguments("--headless=new"); // Use new headless mode
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Anti-bot detection bypass
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // User agent
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");

        return new ChromeDriver(options);
    }

    // Helper methods for data extraction
    private String extractText(WebElement parent, String... selectors) {
        for (String selector : selectors) {
            try {
                WebElement element = parent.findElement(By.cssSelector(selector));
                String text = element.getText().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            } catch (Exception e) {
                // Try next selector
            }
        }
        return "N/A";
    }

    private double extractPrice(WebElement parent, String... selectors) {
        String priceText = extractText(parent, selectors)
                .replaceAll("[^0-9.]", "");
        try {
            return priceText.isEmpty() ? 0.0 : Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double extractRating(WebElement parent, String... selectors) {
        String ratingText = extractText(parent, selectors)
                .replaceAll("[^0-9.]", "");
        try {
            return ratingText.isEmpty() ? 0.0 : Double.parseDouble(ratingText);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
