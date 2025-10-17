package com.ecommerce.analyzer.util;

import javafx.scene.Scene;

/**
 * Theme Manager
 * Manages application themes (light/dark)
 */
public class ThemeManager {

    private static String currentTheme = "light";

    // CSS file paths
    private static final String LIGHT_THEME_CSS = "/css/light-theme.css";
    private static final String DARK_THEME_CSS = "/css/dark-theme.css";

    /**
     * Apply theme to a scene
     * @param scene JavaFX Scene
     * @param theme Theme name (light/dark)
     */
    public static void applyTheme(Scene scene, String theme) {
        currentTheme = theme;
        scene.getStylesheets().clear();

        String cssPath = theme.equals("dark") ? DARK_THEME_CSS : LIGHT_THEME_CSS;

        try {
            String css = ThemeManager.class.getResource(cssPath).toExternalForm();
            scene.getStylesheets().add(css);
            System.out.println("Applied theme: " + theme);
        } catch (Exception e) {
            System.err.println("Error loading theme CSS: " + e.getMessage());
        }
    }

    /**
     * Toggle between light and dark themes
     * @param scene JavaFX Scene
     */
    public static void toggleTheme(Scene scene) {
        String newTheme = currentTheme.equals("light") ? "dark" : "light";
        applyTheme(scene, newTheme);
    }

    /**
     * Get current theme
     * @return Current theme name
     */
    public static String getCurrentTheme() {
        return currentTheme;
    }
}
