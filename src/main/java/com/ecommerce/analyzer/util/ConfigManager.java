package com.ecommerce.analyzer.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration Manager
 * Manages application configuration from properties file
 */
public class ConfigManager {
    private static Properties properties;

    // Static block to load properties once
    static {
        try {
            properties = new Properties();
            InputStream input = ConfigManager.class.getClassLoader()
                    .getResourceAsStream("application.properties");

            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }

            properties.load(input);
            System.out.println("Application properties loaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load application properties", e);
        }
    }

    /**
     * Get property value by key
     * @param key Property key
     * @return Property value
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Get property with default value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get Google Gemini API Key
     * @return Gemini API key
     */
    public static String getGeminiApiKey() {
        return properties.getProperty("gemini.api.key");
    }

    /**
     * Get auto-refresh interval in minutes
     * @return Refresh interval
     */
    public static int getAutoRefreshInterval() {
        String interval = properties.getProperty("auto.refresh.interval", "60");
        return Integer.parseInt(interval);
    }

    /**
     * Get current theme setting
     * @return Theme name (light/dark)
     */
    public static String getTheme() {
        return properties.getProperty("app.theme", "dark");
    }
}
