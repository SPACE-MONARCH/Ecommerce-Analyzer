package com.ecommerce.analyzer;

import com.ecommerce.analyzer.controller.MainController;
import com.ecommerce.analyzer.util.ConfigManager;
import com.ecommerce.analyzer.util.DBUtil;
import com.ecommerce.analyzer.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main Application Class
 * Entry point for the E-commerce Product Analyzer
 */
public class Main extends Application {

    private MainController controller;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Test database connection
            System.out.println("Testing database connection...");
            if (DBUtil.testConnection()) {
                System.out.println("✓ Database connected successfully");
            } else {
                System.err.println("✗ Database connection failed!");
                showErrorAndExit("Database Connection Failed",
                        "Could not connect to PostgreSQL database.\n" +
                                "Please check your database configuration in application.properties");
                return;
            }

            // Load FXML
            System.out.println("Loading UI...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            controller = loader.getController();

            // Create scene
            Scene scene = new Scene(root, 1400, 900);

            // Apply theme
            String initialTheme = ConfigManager.getTheme();
            ThemeManager.applyTheme(scene, initialTheme);

            // Setup stage
            primaryStage.setTitle("STOP B4U SHOP - Product Comparison Analyzer");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(700);

            // Set application icon (optional - will use default if not found)
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
            } catch (Exception e) {
                System.out.println("Application icon not found, using default");
            }

            // Handle window close
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Shutting down application...");
                if (controller != null) {
                    controller.shutdown();
                }
                System.exit(0);
            });

            primaryStage.show();
            System.out.println("✓ Application started successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndExit("Application Error",
                    "Failed to start application: " + e.getMessage());
        }
    }

    /**
     * Show error dialog and exit
     */
    private void showErrorAndExit(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }

    /**
     * Application entry point
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("E-commerce Product Analyzer");
        System.out.println("========================================");
        launch(args);
    }
}
