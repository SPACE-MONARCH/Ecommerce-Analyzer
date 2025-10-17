package com.ecommerce.analyzer.service;

import com.ecommerce.analyzer.model.ProductDetail;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.DomNodeList;
import org.w3c.dom.Node;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlUnitScraperService {

    // Suppress HtmlUnit verbose logging
    static {
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
    }

    private static final int TIMEOUT_MS = 15000;
    private static final int JS_WAIT_MS = 5000;

    /**
     * Scrapes product details from Flipkart using HtmlUnit
     */
    public ProductDetail scrapeFlipkart(String productName, Long productId) {
        System.out.println("[HtmlUnit] Scraping Flipkart for: " + productName);

        WebClient webClient = createWebClient(BrowserVersion.CHROME);

        try {
            // Add delay to avoid rate limiting
            Thread.sleep(3000);

            String encodedSearch = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String url = "https://www.flipkart.com/search?q=" + encodedSearch;

            HtmlPage page = webClient.getPage(url);

            // Critical: Wait for JavaScript to execute
            webClient.waitForBackgroundJavaScript(JS_WAIT_MS);

            // Try multiple selectors (Flipkart changes these frequently)
            DomNodeList<DomNode> products = page.querySelectorAll("div[data-id]");
            if (products.isEmpty()) {
                products = page.querySelectorAll("div._1AtVbE");
            }
            if (products.isEmpty()) {
                products = page.querySelectorAll("div.cPHDOP");
            }

            if (products.isEmpty()) {
                System.out.println("[HtmlUnit] No products found for Flipkart. Possible blocking.");
                return null;
            }

            DomNode firstProduct = products.get(0);

            ProductDetail detail = new ProductDetail();
            detail.setProductId(productId);
            detail.setPlatform("Flipkart");
            detail.setProductLink(url);

            // Extract product details
            detail.setPrice(extractPrice(firstProduct, "div._30jeq3, div._3I9_wc"));
            detail.setRating(extractRating(firstProduct, "div._3LWZlK"));
            detail.setReviewCount(extractReviewCount(firstProduct, "span._2_R_DZ"));
            detail.setSeller(extractText(firstProduct, "div._2WkVRV"));
            detail.setDeliveryTime("Check website");
            detail.setReturnPolicy("10 days return policy");
            detail.setAvailability("In Stock");

            System.out.println("[HtmlUnit] ✓ Flipkart scraping successful");
            return detail;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[HtmlUnit] Interrupted: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("[HtmlUnit] Flipkart scraping failed: " + e.getMessage());
            return null;
        } finally {
            webClient.close();
        }
    }

    /**
     * Scrapes product details from Amazon using HtmlUnit
     */
    public ProductDetail scrapeAmazon(String productName, Long productId) {
        System.out.println("[HtmlUnit] Scraping Amazon for: " + productName);

        WebClient webClient = createWebClient(BrowserVersion.CHROME);

        try {
            Thread.sleep(4000); // Longer delay for Amazon (more aggressive)

            String encodedSearch = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String url = "https://www.amazon.in/s?k=" + encodedSearch;

            HtmlPage page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(JS_WAIT_MS);

            // Amazon product selectors
            DomNodeList<DomNode> products = page.querySelectorAll("div[data-component-type='s-search-result']");

            if (products.isEmpty()) {
                System.out.println("[HtmlUnit] No products found for Amazon. Likely blocked.");
                return null;
            }

            DomNode firstProduct = products.get(0);

            ProductDetail detail = new ProductDetail();
            detail.setProductId(productId);
            detail.setPlatform("Amazon");
            detail.setProductLink(url);

            detail.setPrice(extractPrice(firstProduct, "span.a-price-whole"));
            detail.setRating(extractRating(firstProduct, "span.a-icon-alt"));
            detail.setSeller(extractText(firstProduct, "span.a-size-base"));
            detail.setDeliveryTime("2-3 days");
            detail.setReturnPolicy("30 days return");
            detail.setAvailability("In Stock");

            System.out.println("[HtmlUnit] ✓ Amazon scraping successful");
            return detail;

        } catch (Exception e) {
            System.err.println("[HtmlUnit] Amazon scraping failed: " + e.getMessage());
            return null;
        } finally {
            webClient.close();
        }
    }

    /**
     * Creates configured WebClient for scraping
     */
    private WebClient createWebClient(BrowserVersion browser) {
        WebClient client = new WebClient(browser);

        // Critical configurations for anti-bot bypass
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setCssEnabled(false); // Faster scraping
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setPrintContentOnFailingStatusCode(false);
        client.getOptions().setRedirectEnabled(true);
        client.getOptions().setTimeout(TIMEOUT_MS);
        client.getOptions().setUseInsecureSSL(true);

        // Mimic real browser behavior
        client.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9");
        client.addRequestHeader("Accept-Language", "en-US,en;q=0.9");
        client.addRequestHeader("Accept-Encoding", "gzip, deflate, br");
        client.addRequestHeader("Connection", "keep-alive");

        return client;
    }

    // Helper methods for data extraction
    private String extractText(DomNode parent, String cssQuery) {
        try {
            DomNode node = parent.querySelector(cssQuery);
            return node != null ? node.getTextContent().trim() : "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private double extractPrice(DomNode parent, String cssQuery) {
        String priceText = extractText(parent, cssQuery)
                .replaceAll("[^0-9.]", ""); // Keep only numbers and decimal
        try {
            return priceText.isEmpty() ? 0.0 : Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double extractRating(DomNode parent, String cssQuery) {
        String ratingText = extractText(parent, cssQuery)
                .replaceAll("[^0-9.]", "");
        try {
            return ratingText.isEmpty() ? 0.0 : Double.parseDouble(ratingText);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Integer extractReviewCount(DomNode parent, String cssQuery) {
        String reviewText = extractText(parent, cssQuery)
                .replaceAll("[^0-9]", "");
        try {
            return reviewText.isEmpty() ? 0 : Integer.parseInt(reviewText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
