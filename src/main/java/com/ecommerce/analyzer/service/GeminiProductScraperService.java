package com.ecommerce.analyzer.service;

import com.ecommerce.analyzer.model.ProductDetail;
import com.ecommerce.analyzer.util.ConfigManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced Google Gemini AI scraper for multi-platform product comparison
 * Supports: Amazon, Flipkart, Myntra, Meesho, Ajio, Snapdeal, Nykaa,
 *           Shopify stores, Tata CLiQ, FirstCry
 * Features:
 * - Realistic October 2025 pricing
 * - Category-based platform filtering
 * - Diwali season offers
 * - Graceful fallback to mock data
 * - Smart error handling
 */
public class GeminiProductScraperService {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Enhanced timeout configuration
    private static final int API_TIMEOUT_SECONDS = 30;
    private static final int MAX_RETRIES = 2;
    private final OkHttpClient client;
    private final String apiKey;
    private final Random random = new Random();

    // All supported platforms
    private static final String[] ALL_PLATFORMS = {
            "Amazon", "Flipkart", "Myntra", "Meesho", "Ajio", "Snapdeal",
            "Nykaa", "Tata CLiQ", "FirstCry", "Shopify"
    };

    public GeminiProductScraperService() {
        // Enhanced OkHttpClient with proper timeout configuration
        this.client = new OkHttpClient.Builder()
                .connectTimeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        this.apiKey = ConfigManager.getGeminiApiKey();
        if (apiKey == null || apiKey.contains("YOUR") || apiKey.isEmpty()) {
            System.out.println("[Gemini] ⚠️  API key not configured - will use enhanced mock data");
            System.out.println("[Gemini] Get your free API key at: https://ai.google.dev");
        } else {
            System.out.println("[Gemini] ✓ API key configured successfully");
        }
    }

    /**
     * Gets product data for ALL supported platforms
     * Enhanced with retry logic and better error handling
     */
    public List<ProductDetail> scrapeAllPlatforms(String productName, Long productId) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║    MULTI-PLATFORM PRODUCT DATA RETRIEVAL (AI)        ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("[Gemini Scraper] 🔍 Product: " + productName);
        System.out.println("[Gemini Scraper] 🌐 Available Platforms: " + String.join(", ", ALL_PLATFORMS));

        // Determine which platforms are relevant for this product category
        String[] relevantPlatforms = getRelevantPlatforms(productName);
        System.out.println("[Gemini Scraper] 📊 Relevant Platforms: " + String.join(", ", relevantPlatforms));

        // Try Gemini API with retry logic
        if (apiKey != null && !apiKey.contains("YOUR") && !apiKey.isEmpty()) {
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    System.out.println("\n[Gemini Scraper] 🤖 Attempt " + attempt + "/" + MAX_RETRIES + " - Calling Gemini API...");

                    String prompt = buildEnhancedPrompt(productName, relevantPlatforms);
                    String geminiResponse = callGeminiAPI(prompt);

                    if (geminiResponse != null && !geminiResponse.isEmpty()) {
                        List<ProductDetail> parsed = parseGeminiResponse(geminiResponse, productName, productId);
                        if (parsed != null && !parsed.isEmpty()) {
                            System.out.println("\n[Gemini Scraper] ✓ SUCCESS! Retrieved " + parsed.size() + " platforms");
                            printPlatformSummary(parsed);
                            return parsed;
                        }
                    }

                    if (attempt < MAX_RETRIES) {
                        System.out.println("[Gemini Scraper] ⚠️  Attempt failed, retrying in 2 seconds...");
                        Thread.sleep(2000);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("[Gemini Scraper] ⚠️  Error on attempt " + attempt + ": " + e.getMessage());
                }
            }
            System.out.println("[Gemini Scraper] ✗ All API attempts failed");
        }

        // Fallback: Generate enhanced realistic mock data
        System.out.println("\n[Gemini Scraper] 📦 Generating enhanced mock data...");
        List<ProductDetail> mockData = generateEnhancedMockData(productName, productId, relevantPlatforms);
        System.out.println("[Gemini Scraper] ✓ Generated " + mockData.size() + " platform entries");
        printPlatformSummary(mockData);
        return mockData;
    }

    /** Prints summary of platforms for console visibility */
    private void printPlatformSummary(List<ProductDetail> details) {
        System.out.println("\n┌─────────────────────────────────────────────────────┐");
        System.out.println("│              PLATFORM COMPARISON SUMMARY             │");
        System.out.println("├─────────────┬──────────────┬──────────┬─────────────┤");
        System.out.println("│  Platform   │    Price     │  Rating  │ Availability│");
        System.out.println("├─────────────┼──────────────┼──────────┼─────────────┤");

        for (ProductDetail detail : details) {
            System.out.printf("│ %-11s │ ₹%,10.2f │   %.1f⭐   │ %-11s │%n",
                    detail.getPlatform(),
                    detail.getPrice(),
                    detail.getRating(),
                    detail.getAvailability()
            );
        }
        System.out.println("└─────────────┴──────────────┴──────────┴─────────────┘\n");
    }

    /**
     * Enhanced platform detection with more categories
     */
    private String[] getRelevantPlatforms(String productName) {
        String lower = productName.toLowerCase();

        // Beauty/cosmetics products
        if (lower.contains("lipstick") || lower.contains("makeup") ||
                lower.contains("skincare") || lower.contains("cosmetic") ||
                lower.contains("beauty") || lower.contains("nail polish")) {
            return new String[]{"Amazon", "Flipkart", "Myntra", "Nykaa", "Tata CLiQ", "Meesho"};
        }

        // Baby products
        if (lower.contains("baby") || lower.contains("diaper") ||
                lower.contains("kids") || lower.contains("toy") ||
                lower.contains("infant") || lower.contains("newborn")) {
            return new String[]{"Amazon", "Flipkart", "FirstCry", "Meesho", "Shopify"};
        }

        // Fashion/clothing
        if (lower.contains("shirt") || lower.contains("dress") ||
                lower.contains("jeans") || lower.contains("shoe") ||
                lower.contains("saree") || lower.contains("kurta") ||
                lower.contains("t-shirt") || lower.contains("clothing")) {
            return new String[]{"Amazon", "Flipkart", "Myntra", "Ajio", "Meesho", "Tata CLiQ"};
        }

        // Footwear specific
        if (lower.contains("sneaker") || lower.contains("sandal") ||
                lower.contains("boot") || lower.contains("footwear")) {
            return new String[]{"Amazon", "Flipkart", "Myntra", "Ajio", "Meesho"};
        }

        // Electronics (most common)
        return new String[]{"Amazon", "Flipkart", "Tata CLiQ", "Shopify", "Snapdeal", "Meesho"};
    }

    /**
     * Enhanced prompt with better structure and examples
     */
    private String buildEnhancedPrompt(String productName, String[] platforms) {
        return String.format("""
            You are an expert Indian e-commerce pricing analyst with real-time market knowledge as of October 2025.
            
            TASK: Provide realistic product comparison data for: "%s"
            
            PLATFORMS TO ANALYZE: %s
            
            RESPONSE FORMAT (JSON ONLY, NO OTHER TEXT):
            {
              "platforms": [
                {
                  "platform": "Platform Name",
                  "price": <realistic INR price>,
                  "rating": <3.0 to 5.0>,
                  "reviewCount": <realistic number>,
                  "seller": "<official seller name>",
                  "deliveryTime": "<X-Y days>",
                  "returnPolicy": "<platform policy>",
                  "warranty": "<warranty details>",
                  "offers": "<current offer>",
                  "availability": "<In Stock | Limited Stock | Out of Stock>"
                }
              ]
            }
            
            CRITICAL PRICING RULES:
            1. Base prices on October 2025 Indian market rates
            2. Prices must vary 5-15%% across platforms (realistic competition)
            3. Meesho: 15-25%% cheaper (budget platform)
            4. Amazon/Flipkart: Market average
            5. Tata CLiQ: 3-8%% premium (premium platform)
            6. Diwali season (Oct 2025) = festival offers on all platforms
            
            REALISTIC CONSTRAINTS:
            - Ratings: Not all 4.5+, use realistic distribution
            - Reviews: Vary by platform popularity (Amazon highest)
            - Availability: 75%% in stock, 20%% limited, 5%% out of stock
            - Include ONLY platforms that actually sell this product category
            
            EXAMPLE (for reference):
            iPhone 15 would be: Amazon ₹79,900, Flipkart ₹79,999, Meesho ₹67,500
            
            Respond ONLY with valid JSON. No explanations, no markdown, just JSON.
            """,
                productName,
                String.join(", ", platforms)
        );
    }

    /**
     * Enhanced API call with better error handling
     */
    private String callGeminiAPI(String prompt) throws IOException {
        // Build request
        JsonObject request = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        request.add("contents", contents);

        // Add generation config for better responses
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);  // Balanced creativity
        generationConfig.addProperty("topK", 40);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 2048);
        request.add("generationConfig", generationConfig);

        // Make API call
        String url = GEMINI_API_URL + "?key=" + apiKey;
        RequestBody body = RequestBody.create(request.toString(), JSON);
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("[Gemini Scraper] ⚠️  API HTTP " + response.code() + ": " + errorBody);
                return null;
            }

            String responseBody = response.body().string();

            // Enhanced response parsing
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            // Check for API errors
            if (jsonResponse.has("error")) {
                JsonObject error = jsonResponse.getAsJsonObject("error");
                System.err.println("[Gemini Scraper] ⚠️  API Error: " + error.get("message").getAsString());
                return null;
            }

            JsonArray candidates = jsonResponse.getAsJsonArray("candidates");

            if (candidates != null && candidates.size() > 0) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                JsonObject contentObj = candidate.getAsJsonObject("content");
                JsonArray partsArray = contentObj.getAsJsonArray("parts");

                if (partsArray != null && partsArray.size() > 0) {
                    String text = partsArray.get(0).getAsJsonObject().get("text").getAsString();
                    System.out.println("[Gemini Scraper] ✓ API response received (" + text.length() + " chars)");
                    return extractJSON(text);
                }
            }
        } catch (IOException e) {
            System.err.println("[Gemini Scraper] ⚠️  Network error: " + e.getMessage());
            throw e;
        }

        return null;
    }

    /**
     * Extracts JSON from Gemini response (removes markdown formatting)
     */
    private String extractJSON(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        text = text.trim();
        // Remove markdown code blocks
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        text = text.trim();
        if (!text.startsWith("{") || !text.endsWith("}")) {
            System.err.println("[Gemini Scraper] ⚠️  Response doesn't look like JSON");
            return null;
        }
        return text;
    }


/**
 * Enhanced JSON parsing with better error messages
 */
private List<ProductDetail> parseGeminiResponse(String jsonResponse, String productName, Long productId) {
    if (jsonResponse == null || jsonResponse.isEmpty()) {
        System.err.println("[Gemini Scraper] ⚠️  Empty JSON response");
        return null;
    }

    List<ProductDetail> details = new ArrayList<>();

    try {
        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (!root.has("platforms")) {
            System.err.println("[Gemini Scraper] ⚠️  JSON missing 'platforms' array");
            return null;
        }

        JsonArray platforms = root.getAsJsonArray("platforms");

        if (platforms == null || platforms.size() == 0) {
            System.err.println("[Gemini Scraper] ⚠️  Empty platforms array");
            return null;
        }

        System.out.println("[Gemini Scraper] 📦 Parsing " + platforms.size() + " platforms...");

        for (int i = 0; i < platforms.size(); i++) {
            try {
                JsonObject platformData = platforms.get(i).getAsJsonObject();

                ProductDetail detail = new ProductDetail();
                detail.setProductId(productId);

                // ✅ FIX: Validate platform name
                String platformName = platformData.has("platform") ?
                        platformData.get("platform").getAsString() : "Unknown";

                if (platformName == null || platformName.trim().isEmpty() || platformName.equals("Unknown")) {
                    System.err.println("[Gemini Scraper] ⚠️  Skipping platform with invalid name at index " + i);
                    continue;  // Skip this platform
                }

                detail.setPlatform(platformName);
                detail.setPrice(platformData.get("price").getAsDouble());
                detail.setRating(platformData.get("rating").getAsDouble());
                detail.setReviewCount(platformData.get("reviewCount").getAsInt());
                detail.setSeller(platformData.get("seller").getAsString());
                detail.setDeliveryTime(platformData.get("deliveryTime").getAsString());
                detail.setReturnPolicy(platformData.get("returnPolicy").getAsString());
                detail.setWarranty(platformData.get("warranty").getAsString());
                detail.setOffers(platformData.get("offers").getAsString());
                detail.setAvailability(platformData.get("availability").getAsString());
                detail.setProductLink(generatePlatformLink(detail.getPlatform(), productName));

                details.add(detail);
            } catch (Exception e) {
                System.err.println("[Gemini Scraper] ⚠️  Error parsing platform " + i + ": " + e.getMessage());
            }
        }

        System.out.println("[Gemini Scraper] ✓ Successfully parsed " + details.size() + " platforms");

    } catch (Exception e) {
        System.err.println("[Gemini Scraper] ⚠️  JSON parsing error: " + e.getMessage());
        return null;
    }
    return details;
}


/** Enhanced mock data generation with more realistic pricing */
private List<ProductDetail> generateEnhancedMockData(String productName, Long productId, String[] platforms) {
    List<ProductDetail> details = new ArrayList<>();
    double basePrice = estimateBasePrice(productName);

    for (String platform : platforms) {
        ProductDetail detail = new ProductDetail();
        detail.setProductId(productId);
        detail.setPlatform(platform);

        // Price variations by platform
        double platformPrice = adjustPriceByPlatform(basePrice, platform);
        detail.setPrice(Math.round(platformPrice * 100.0) / 100.0);

        // Realistic rating distribution
        detail.setRating(generateRealisticRating(platform));

        // Review counts based on platform popularity
        detail.setReviewCount(generateReviewCount(platform));

        // Platform-specific details
        detail.setSeller(getRealisticSeller(platform));
        detail.setDeliveryTime(getDeliveryTime(platform));
        detail.setReturnPolicy(getReturnPolicy(platform));
        detail.setWarranty(getWarranty(productName));
        detail.setOffers(getDiwaliOffer(platform));
        detail.setAvailability(getAvailability());
        detail.setProductLink(generatePlatformLink(platform, productName));
        details.add(detail);
    }

    // Sort by price (lowest first) for better UX
    details.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));

    return details;
}

// ========== HELPER METHODS FOR REALISTIC DATA ==========

private double estimateBasePrice(String productName) {
    String lower = productName.toLowerCase();

    // Electronics - Popular models
    if (lower.contains("iphone 16")) return 89900;
    if (lower.contains("iphone 15 pro")) return 134900;
    if (lower.contains("iphone 15")) return 79900;
    if (lower.contains("iphone 14")) return 69900;
    if (lower.contains("iphone 13")) return 59900;
    if (lower.contains("samsung s24 ultra")) return 124999;
    if (lower.contains("samsung s24")) return 74999;
    if (lower.contains("samsung s23")) return 64999;
    if (lower.contains("oneplus 12")) return 64999;
    if (lower.contains("oneplus 11")) return 56999;
    if (lower.contains("pixel 8")) return 75999;

    // General electronics
    if (lower.contains("laptop")) return 50000 + random.nextDouble() * 50000;
    if (lower.contains("macbook")) return 95000 + random.nextDouble() * 100000;
    if (lower.contains("airpods")) return 12000 + random.nextDouble() * 12000;
    if (lower.contains("watch") || lower.contains("smartwatch")) return 5000 + random.nextDouble() * 20000;
    if (lower.contains("tv") || lower.contains("television")) return 25000 + random.nextDouble() * 75000;
    if (lower.contains("tablet") || lower.contains("ipad")) return 25000 + random.nextDouble() * 75000;

    // Fashion
    if (lower.contains("nike") || lower.contains("adidas")) return 3000 + random.nextDouble() * 7000;
    if (lower.contains("shoe") || lower.contains("sneaker")) return 2000 + random.nextDouble() * 8000;
    if (lower.contains("shirt") || lower.contains("tshirt")) return 400 + random.nextDouble() * 1600;
    if (lower.contains("jeans") || lower.contains("pants")) return 800 + random.nextDouble() * 2200;
    if (lower.contains("dress")) return 1000 + random.nextDouble() * 4000;
    if (lower.contains("saree")) return 1500 + random.nextDouble() * 8500;

    // Beauty
    if (lower.contains("lipstick")) return 300 + random.nextDouble() * 1700;
    if (lower.contains("perfume")) return 1500 + random.nextDouble() * 8500;
    if (lower.contains("skincare")) return 500 + random.nextDouble() * 2500;

    // Baby
    if (lower.contains("diaper")) return 800 + random.nextDouble() * 1200;
    if (lower.contains("baby")) return 500 + random.nextDouble() * 4500;

    // Default
    return 1000 + random.nextDouble() * 9000;
}

private double adjustPriceByPlatform(double basePrice, String platform) {
    double multiplier = switch (platform) {
        case "Meesho" -> 0.75 + random.nextDouble() * 0.10;
        case "Snapdeal" -> 0.82 + random.nextDouble() * 0.12;
        case "Amazon" -> 0.95 + random.nextDouble() * 0.10;
        case "Flipkart" -> 0.93 + random.nextDouble() * 0.12;
        case "Myntra" -> 0.97 + random.nextDouble() * 0.08;
        case "Tata CLiQ" -> 0.98 + random.nextDouble() * 0.07;
        case "Nykaa" -> 0.96 + random.nextDouble() * 0.09;
        case "Ajio" -> 0.94 + random.nextDouble() * 0.11;
        case "FirstCry" -> 0.92 + random.nextDouble() * 0.13;
        case "Shopify" -> 0.90 + random.nextDouble() * 0.15;
        default -> 0.95 + random.nextDouble() * 0.10;
    };
    return basePrice * multiplier;
}

private double generateRealisticRating(String platform) {
    double base = switch (platform) {
        case "Amazon", "Flipkart" -> 4.0 + random.nextDouble() * 0.8;
        case "Myntra", "Tata CLiQ", "Nykaa" -> 3.8 + random.nextDouble() * 1.0;
        case "Meesho", "Snapdeal" -> 3.5 + random.nextDouble() * 1.2;
        default -> 3.7 + random.nextDouble() * 1.0;
    };
    return Math.round(base * 10.0) / 10.0;
}

private int generateReviewCount(String platform) {
    return switch (platform) {
        case "Amazon" -> 1000 + random.nextInt(10000);
        case "Flipkart" -> 500 + random.nextInt(8000);
        case "Myntra" -> 200 + random.nextInt(3000);
        case "Meesho" -> 100 + random.nextInt(2000);
        case "Nykaa" -> 150 + random.nextInt(2500);
        default -> 50 + random.nextInt(1500);
    };
}

private String getRealisticSeller(String platform) {
    return switch (platform) {
        case "Amazon" -> random.nextBoolean() ? "Amazon Retail" : "Cloudtail India";
        case "Flipkart" -> "Flipkart Assured";
        case "Myntra" -> "Myntra Fashion Store";
        case "Meesho" -> "Meesho Supplier";
        case "Ajio" -> "AJIO Retail";
        case "Nykaa" -> "Nykaa Fashion";
        case "Tata CLiQ" -> "Tata CLiQ";
        case "FirstCry" -> "FirstCry Store";
        case "Snapdeal" -> "Snapdeal Seller";
        case "Shopify" -> "Brand Official Store";
        default -> platform + " Official";
    };
}

private String getDeliveryTime(String platform) {
    return switch (platform) {
        case "Amazon" -> random.nextBoolean() ? "1-2 days" : "2-3 days";
        case "Flipkart" -> "2-3 days";
        case "Meesho" -> "3-5 days";
        case "Snapdeal" -> "4-6 days";
        default -> (2 + random.nextInt(3)) + "-" + (3 + random.nextInt(3)) + " days";
    };
}

private String getReturnPolicy(String platform) {
    return switch (platform) {
        case "Amazon" -> "30 days return & refund";
        case "Flipkart", "Snapdeal" -> "10 days return policy";
        case "Myntra", "Tata CLiQ" -> "30 days easy return";
        case "Nykaa" -> "15 days return for sealed products";
        case "Meesho" -> "7 days return available";
        case "FirstCry" -> "15 days easy return";
        default -> "14 days return policy";
    };
}

private String getWarranty(String productName) {
    String lower = productName.toLowerCase();
    if (lower.contains("iphone") || lower.contains("samsung") || lower.contains("laptop")) {
        return "1 year manufacturer warranty";
    } else if (lower.contains("watch") || lower.contains("tv")) {
        return "1 year warranty + 1 year extended";
    } else if (lower.contains("shirt") || lower.contains("shoe") || lower.contains("dress")) {
        return "No warranty (fashion item)";
    } else if (lower.contains("lipstick") || lower.contains("cream")) {
        return "Authentic product guarantee";
    }
    return "6 months warranty";
}

private String getDiwaliOffer(String platform) {
    String[] offers = {
            "🎉 Diwali Sale: Extra 10% off",
            "🪔 Festive Offer: Flat 15% discount",
            "✨ Diwali Special: Buy 1 Get 1 Free",
            "🎊 Festival Deal: Up to 20% off",
            "💳 Bank Offer: 10% instant discount",
            "🎁 Diwali Bonanza: No Cost EMI",
            "🌟 Festive Savings: Cashback ₹500",
            "🔥 Diwali Dhamaka: Extra 12% off",
            "🎇 Limited Time: Flat ₹1000 off",
            "💰 Festival Special: 5% cashback"
    };
    return offers[random.nextInt(offers.length)];
}

private String getAvailability() {
    int rand = random.nextInt(100);
    if (rand < 75) return "In Stock";
    if (rand < 95) return "Limited Stock";
    return "Out of Stock";
}

private String generatePlatformLink(String platform, String productName) {
    String encoded = productName.toLowerCase().replace(" ", "+");
    return switch (platform.toLowerCase()) {
        case "amazon" -> "https://www.amazon.in/s?k=" + encoded;
        case "flipkart" -> "https://www.flipkart.com/search?q=" + encoded;
        case "myntra" -> "https://www.myntra.com/" + encoded;
        case "meesho" -> "https://www.meesho.com/search?q=" + encoded;
        case "ajio" -> "https://www.ajio.com/search?query=" + encoded;
        case "snapdeal" -> "https://www.snapdeal.com/search?keyword=" + encoded;
        case "nykaa" -> "https://www.nykaa.com/search/result/?q=" + encoded;
        case "tata cliq" -> "https://www.tatacliq.com/search/?searchCategory=all&text=" + encoded;
        case "firstcry" -> "https://www.firstcry.com/search?q=" + encoded;
        case "shopify" -> "https://shop.app/search?query=" + encoded;
        default -> "https://www.google.com/search?q=" + encoded;
    };
}
}
