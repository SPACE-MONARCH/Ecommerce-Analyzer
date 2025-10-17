package com.ecommerce.analyzer.service;

import com.ecommerce.analyzer.model.ProductDetail;
import com.ecommerce.analyzer.util.ConfigManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.util.List;

/**
 * AI Analyzer Service
 * Uses Google Gemini API for intelligent product analysis
 */
public class AIAnalyzerService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private final OkHttpClient client;
    private final String apiKey;

    public AIAnalyzerService() {
        this.client = new OkHttpClient();
        this.apiKey = ConfigManager.getGeminiApiKey();
    }

    /**
     * Analyze products and recommend best platform
     */
    public String analyzeAndRecommend(List<ProductDetail> productDetails) {
        if (productDetails == null || productDetails.isEmpty()) {
            return "No product data available for analysis.";
        }

        // NEW: Check if API key is configured
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("YOUR")) {
            System.out.println("AI Analyzer: No API key configured, using simple recommendation");
            return getSimpleRecommendation(productDetails);
        }

        try {
            String prompt = buildAnalysisPrompt(productDetails);
            String response = callGeminiAPI(prompt);
            return response;
        } catch (Exception e) {
            System.err.println("AI Analysis error: " + e.getMessage());
            // NEW: Fallback to simple recommendation instead of error message
            return getSimpleRecommendation(productDetails);
        }
    }

    /**
     * Build prompt for Gemini
     */
    private String buildAnalysisPrompt(List<ProductDetail> details) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following product comparison data from different e-commerce platforms and recommend the best platform to buy from. ");
        prompt.append("Consider price, rating, delivery time, return policy, warranty, and offers.\n\n");

        for (ProductDetail detail : details) {
            prompt.append(String.format("Platform: %s\n", detail.getPlatform()));
            prompt.append(String.format("Price: ₹%.2f\n", detail.getPrice()));
            prompt.append(String.format("Rating: %.1f/5\n", detail.getRating()));
            prompt.append(String.format("Seller: %s\n", detail.getSeller()));
            prompt.append(String.format("Delivery: %s\n", detail.getDeliveryTime()));
            prompt.append(String.format("Return Policy: %s\n", detail.getReturnPolicy()));
            prompt.append(String.format("Warranty: %s\n", detail.getWarranty()));
            prompt.append(String.format("Offers: %s\n\n", detail.getOffers()));
        }

        prompt.append("Provide a recommendation in this format:\n");
        prompt.append("**Recommended Platform:** [Platform Name]\n\n");
        prompt.append("**Reasons:**\n");
        prompt.append("1. [First reason]\n");
        prompt.append("2. [Second reason]\n");
        prompt.append("3. [Third reason]\n\n");
        prompt.append("**Overall Value Score:** [Score out of 10]");

        return prompt.toString();
    }

    /**
     * Call Google Gemini API
     */
    private String callGeminiAPI(String prompt) throws Exception {
        // Build request JSON
        JsonObject requestBody = new JsonObject();
        JsonObject contents = new JsonObject();
        JsonObject parts = new JsonObject();
        parts.addProperty("text", prompt);

        contents.add("parts", new com.google.gson.JsonArray());
        contents.getAsJsonArray("parts").add(parts);

        requestBody.add("contents", new com.google.gson.JsonArray());
        requestBody.getAsJsonArray("contents").add(contents);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + apiKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("API call failed: " + response.code());
            }

            String responseBody = response.body().string();
            return parseGeminiResponse(responseBody);
        }
    }

    /**
     * Parse Gemini API response
     */
    private String parseGeminiResponse(String responseJson) {
        try {
            JsonObject jsonObject = JsonParser.parseString(responseJson).getAsJsonObject();
            return jsonObject.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response: " + e.getMessage());
            return "Failed to parse AI response";
        }
    }

    /**
     * Get simple recommendation based on rules
     * Fallback if AI fails
     */
    public String getSimpleRecommendation(List<ProductDetail> details) {
        if (details == null || details.isEmpty()) {
            return "No data available";
        }

        StringBuilder recommendation = new StringBuilder();
        recommendation.append("📊 QUICK ANALYSIS\n\n");

        // Best price
        ProductDetail bestPrice = details.stream()
                .min((d1, d2) -> Double.compare(d1.getPrice(), d2.getPrice()))
                .orElse(details.get(0));

        // Best rating
        ProductDetail bestRating = details.stream()
                .max((d1, d2) -> Double.compare(d1.getRating(), d2.getRating()))
                .orElse(details.get(0));

        recommendation.append(String.format("💰 BEST PRICE: %s at ₹%.2f\n",
                bestPrice.getPlatform(), bestPrice.getPrice()));

        if (bestPrice.getOffers() != null && !bestPrice.getOffers().isEmpty()) {
            recommendation.append("   Offer: ").append(bestPrice.getOffers()).append("\n");
        }
        recommendation.append("\n");

        recommendation.append(String.format("⭐ BEST RATED: %s with %.1f stars\n",
                bestRating.getPlatform(), bestRating.getRating()));

        if (bestRating != bestPrice) {
            double priceDiff = bestRating.getPrice() - bestPrice.getPrice();
            recommendation.append(String.format("   Price: ₹%.2f (₹%.2f more)\n\n",
                    bestRating.getPrice(), priceDiff));
        } else {
            recommendation.append("   (Also the cheapest option!)\n\n");
        }

        recommendation.append("💡 RECOMMENDATION:\n");
        if (bestPrice == bestRating) {
            recommendation.append(String.format("   Buy from %s - Best price AND top rated!\n",
                    bestPrice.getPlatform()));
        } else {
            double priceDiffPercent = ((bestRating.getPrice() - bestPrice.getPrice()) / bestPrice.getPrice()) * 100;
            if (priceDiffPercent < 10) {
                recommendation.append("   Consider the better-rated option for slightly more.\n");
            } else {
                recommendation.append("   Go with the cheapest if price is your priority.\n");
            }
        }
        return recommendation.toString();
    }
}