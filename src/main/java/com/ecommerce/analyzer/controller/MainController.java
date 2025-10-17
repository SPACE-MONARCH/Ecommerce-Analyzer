package com.ecommerce.analyzer.controller;
import com.ecommerce.analyzer.model.Product;
import com.ecommerce.analyzer.model.ProductDetail;
import com.ecommerce.analyzer.model.PriceHistory;
import com.ecommerce.analyzer.service.*;
import com.ecommerce.analyzer.util.ThemeManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    // Search and navigation
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addUrlButton;
    @FXML private Button toggleLeftSidebar;
    @FXML private Button toggleRightSidebar;

    // Sidebars
    @FXML private VBox leftSidebar;
    @FXML private VBox rightSidebar;
    private boolean leftSidebarVisible = true;
    private boolean rightSidebarVisible = true;

    // Platform filter checkboxes
    @FXML private CheckBox amazonCheck;
    @FXML private CheckBox flipkartCheck;
    @FXML private CheckBox myntraCheck;
    @FXML private CheckBox meeshoCheck;
    @FXML private CheckBox ajioCheck;
    @FXML private CheckBox snapdealCheck;

    // Comparison table
    @FXML private TableView<Map<String, String>> comparisonTable;
    @FXML private TableColumn<Map<String, String>, String> attributeColumn;

    // Quick insights
    @FXML private Label bestPriceLabel;
    @FXML private Label bestRatingLabel;
    @FXML private Label fastestDeliveryLabel;

    // Right sidebar components
    @FXML private TextArea aiRecommendationArea;
    @FXML private LineChart<String, Number> priceChart;

    // Control buttons
    @FXML private Button exportPdfButton;
    @FXML private Button exportExcelButton;
    @FXML private Button refreshButton;
    @FXML private ToggleButton themeToggle;
    @FXML private Label statusLabel;

    // Services
    private final ProductService productService = new ProductService();
    private final AIAnalyzerService aiService = new AIAnalyzerService();
    private final ExportService exportService = new ExportService();
    private final AutoRefreshService autoRefreshService = new AutoRefreshService();
    // Data
    private List<ProductDetail> productDetails = new ArrayList<>();
    private Product currentProduct;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupEventHandlers();
        setupPlatformFilters();
        setupChart();
        setupSidebarToggles();
        autoRefreshService.start();
        setStatus("Ready");
    }
    private void setupTableColumns() {
        attributeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get("attribute")));
    }
    private void setupEventHandlers() {
        searchButton.setOnAction(event -> loadProductComparison());
        addUrlButton.setOnAction(event -> addProductByUrl());
        exportPdfButton.setOnAction(event -> exportToPDF());
        exportExcelButton.setOnAction(event -> exportToExcel());
        refreshButton.setOnAction(event -> refreshData());
        themeToggle.setOnAction(event -> toggleTheme());
        searchField.setOnAction(event -> loadProductComparison());
    }
    private void setupPlatformFilters() {
        amazonCheck.setOnAction(e -> filterPlatforms());
        flipkartCheck.setOnAction(e -> filterPlatforms());
        myntraCheck.setOnAction(e -> filterPlatforms());
        meeshoCheck.setOnAction(e -> filterPlatforms());
        ajioCheck.setOnAction(e -> filterPlatforms());
        snapdealCheck.setOnAction(e -> filterPlatforms());
    }
    private void setupChart() {
        priceChart.setAnimated(true);
        priceChart.setCreateSymbols(false);
    }
    private void setupSidebarToggles() {
        toggleLeftSidebar.setOnAction(e -> toggleLeftSidebarVisibility());
        toggleRightSidebar.setOnAction(e -> toggleRightSidebarVisibility());
    }
    private void toggleLeftSidebarVisibility() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), leftSidebar);

        if (leftSidebarVisible) {
            transition.setToX(-leftSidebar.getWidth());
            leftSidebar.setManaged(false);
        } else {
            transition.setToX(0);
            leftSidebar.setManaged(true);
        }

        transition.play();
        leftSidebarVisible = !leftSidebarVisible;
    }
    private void toggleRightSidebarVisibility() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), rightSidebar);

        if (rightSidebarVisible) {
            transition.setToX(rightSidebar.getWidth());
            rightSidebar.setManaged(false);
        } else {
            transition.setToX(0);
            rightSidebar.setManaged(true);
        }

        transition.play();
        rightSidebarVisible = !rightSidebarVisible;
    }
    private void filterPlatforms() {
        if (productDetails.isEmpty()) return;

        List<ProductDetail> filtered = productDetails.stream()
                .filter(detail -> {
                    String platform = detail.getPlatform().toLowerCase();
                    if (platform.contains("amazon")) return amazonCheck.isSelected();
                    if (platform.contains("flipkart")) return flipkartCheck.isSelected();
                    if (platform.contains("myntra")) return myntraCheck.isSelected();
                    if (platform.contains("meesho")) return meeshoCheck.isSelected();
                    if (platform.contains("ajio")) return ajioCheck.isSelected();
                    if (platform.contains("snapdeal")) return snapdealCheck.isSelected();
                    return true;
                })
                .collect(Collectors.toList());

        displayComparisonTable(filtered);
        updateInsights(filtered);
    }
    @FXML
    private void loadProductComparison() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            showAlert("Input Required", "Please enter a product name", Alert.AlertType.WARNING);
            return;
        }

        setStatus("🔍 Searching for: " + searchTerm + "...");

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Product> products = productService.searchProducts(searchTerm);

                // If product not found in database, auto-add and scrape it
                if (products.isEmpty()) {
                    Platform.runLater(() -> {
                        setStatus("🤖 Product not found. Fetching data from all platforms...");
                    });

                    // Auto-add the product and trigger scraping
                    String dummyUrl = "https://search/" + searchTerm.toLowerCase().replace(" ", "+");
                    Product newProduct = productService.addProductByUrl(dummyUrl, searchTerm);

                    // Now fetch the product details that were just scraped
                    List<ProductDetail> details = productService.getProductDetails(newProduct.getId());
                    currentProduct = newProduct;

                    Platform.runLater(() -> {
                        if (details.isEmpty()) {
                            showAlert("No Data",
                                    "Product added but no platform data available. Try refreshing.",
                                    Alert.AlertType.WARNING);
                            setStatus("Product added - waiting for data");
                        } else {
                            productDetails = details;
                            displayComparisonTable(details);
                            updateInsights(details);
                            loadPriceChart(details);
                            getAIRecommendation(details);
                            setStatus("✓ Loaded " + details.size() + " platform comparisons for: " + searchTerm);
                        }
                    });
                    return null;
                }

                // Product exists - load its details
                currentProduct = products.get(0);
                List<ProductDetail> details = productService.getProductDetails(currentProduct.getId());

                Platform.runLater(() -> {
                    productDetails = details;
                    displayComparisonTable(details);
                    updateInsights(details);
                    loadPriceChart(details);
                    getAIRecommendation(details);
                    setStatus("✓ Loaded " + details.size() + " platform comparisons");
                });
                return null;
            }
        };

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            showAlert("Error",
                    "Failed to load comparison: " + (exception != null ? exception.getMessage() : "Unknown error"),
                    Alert.AlertType.ERROR);
            setStatus("✗ Error occurred");
            exception.printStackTrace();
        });

        new Thread(task).start();
    }

    private void displayComparisonTable(List<ProductDetail> details) {
        // Clear existing columns except attribute column
        comparisonTable.getColumns().clear();
        comparisonTable.getColumns().add(attributeColumn);

        // Create platform columns dynamically
        for (ProductDetail detail : details) {
            TableColumn<Map<String, String>, String> platformColumn = new TableColumn<>(detail.getPlatform());
            platformColumn.setPrefWidth(150);
            platformColumn.setSortable(false);
            final String platform = detail.getPlatform();
            platformColumn.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().get(platform)));

            // Add Buy button as last row cell factory
            platformColumn.setCellFactory(column -> new TableCell<Map<String, String>, String>() {
                private final Button buyButton = new Button("Buy Now");
                {
                    buyButton.getStyleClass().add("table-buy-button");
                    buyButton.setMaxWidth(Double.MAX_VALUE);
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() != 7) { // Row 7 is Buy button row
                        setText(item);
                        setGraphic(null);
                    } else {
                        setText(null);
                        buyButton.setOnAction(e -> {
                            ProductDetail matchingDetail = details.stream()
                                    .filter(d -> d.getPlatform().equals(platform))
                                    .findFirst()
                                    .orElse(null);
                            if (matchingDetail != null) {
                                openURL(matchingDetail.getProductLink());
                            }
                        });
                        setGraphic(buyButton);
                    }
                }
            });
            comparisonTable.getColumns().add(platformColumn);
        }

        // Create rows data
        ObservableList<Map<String, String>> rows = FXCollections.observableArrayList();

        // Row data
        String[] attributes = {"Price", "Rating", "Seller", "Delivery", "Return Policy", "Warranty", "Offers", "Action"};

        for (String attr : attributes) {
            Map<String, String> row = new HashMap<>();
            row.put("attribute", attr);
            for (ProductDetail detail : details) {
                String value = "";
                switch (attr) {
                    case "Price":
                        value = String.format("₹%.2f", detail.getPrice());
                        break;
                    case "Rating":
                        value = String.format("%.1f stars", detail.getRating());
                        break;
                    case "Seller":
                        value = detail.getSeller();
                        break;
                    case "Delivery":
                        value = detail.getDeliveryTime();
                        break;
                    case "Return Policy":
                        value = detail.getReturnPolicy();
                        break;
                    case "Warranty":
                        value = detail.getWarranty();
                        break;
                    case "Offers":
                        value = detail.getOffers();
                        break;
                    case "Action":
                        value = ""; // Button will be rendered by cell factory
                        break;
                }
                row.put(detail.getPlatform(), value);
            }
            rows.add(row);
        }
        comparisonTable.setItems(rows);
    }
    @FXML
    private void addProductByUrl() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Product");
        dialog.setHeaderText("Enter Product Details");
        dialog.setContentText("Product Name:");

        dialog.showAndWait().ifPresent(productName -> {
            if (productName.trim().isEmpty()) {
                showAlert("Error", "Product name cannot be empty", Alert.AlertType.ERROR);
                return;
            }

            // Update status
            setStatus("🔄 Fetching data from all platforms...");

            Task<Product> task = new Task<Product>() {
                @Override
                protected Product call() throws Exception {
                    // Add product with dummy URL (we only need the name)
                    String dummyUrl = "https://search/" + productName.toLowerCase().replace(" ", "+");
                    Product product = productService.addProductByUrl(dummyUrl, productName);

                    // This now triggers multi-platform scraping via Gemini
                    // The productService.addProductByUrl already calls scrapeAllPlatformDetails

                    return product;
                }
            };

            task.setOnSucceeded(event -> {
                Product addedProduct = task.getValue();
                showAlert("Success",
                        "Product added successfully!\nFetching comparison data from multiple platforms...",
                        Alert.AlertType.INFORMATION);

                // Update search field and load comparison
                searchField.setText(productName);

                // Small delay to let database save complete
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(() -> loadProductComparison());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });

            task.setOnFailed(event -> {
                Throwable exception = task.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                showAlert("Error",
                        "Failed to add product: " + errorMessage,
                        Alert.AlertType.ERROR);
                setStatus("Failed to add product");
            });

            new Thread(task).start();
        });
    }

    private void updateInsights(List<ProductDetail> details) {
        if (details.isEmpty()) {
            bestPriceLabel.setText("N/A");
            bestRatingLabel.setText("N/A");
            fastestDeliveryLabel.setText("N/A");
            return;
        }
        ProductDetail bestPrice = details.stream()
                .min((d1, d2) -> Double.compare(d1.getPrice(), d2.getPrice()))
                .orElse(details.get(0));
        bestPriceLabel.setText(String.format("%s - ₹%.2f", bestPrice.getPlatform(), bestPrice.getPrice()));
        ProductDetail bestRating = details.stream()
                .max((d1, d2) -> Double.compare(d1.getRating(), d2.getRating()))
                .orElse(details.get(0));
        bestRatingLabel.setText(String.format("%s - %.1f stars", bestRating.getPlatform(), bestRating.getRating()));
        fastestDeliveryLabel.setText(details.get(0).getPlatform() + " - " + details.get(0).getDeliveryTime());
    }
    private void loadPriceChart(List<ProductDetail> details) {
        priceChart.getData().clear();
        for (ProductDetail detail : details) {
            Task<List<PriceHistory>> task = new Task<List<PriceHistory>>() {
                @Override
                protected List<PriceHistory> call() throws Exception {
                    return productService.getPriceHistory(detail.getId(), 30);
                }
            };
            task.setOnSucceeded(event -> {
                List<PriceHistory> history = task.getValue();
                if (!history.isEmpty()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(detail.getPlatform());
                    for (PriceHistory ph : history) {
                        String date = ph.getRecordedAt().toLocalDate().toString();
                        series.getData().add(new XYChart.Data<>(date, ph.getPrice()));
                    }
                    Platform.runLater(() -> priceChart.getData().add(series));
                }
            });
            new Thread(task).start();
        }
    }

    private void getAIRecommendation(List<ProductDetail> details) {
        aiRecommendationArea.setText("Analyzing with AI...");
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return aiService.analyzeAndRecommend(details);
            }
        };
        task.setOnSucceeded(event -> aiRecommendationArea.setText(task.getValue()));
        task.setOnFailed(event -> {
            String fallback = aiService.getSimpleRecommendation(details);
            aiRecommendationArea.setText("AI analysis unavailable. Simple recommendation:\n\n" + fallback);
        });
        new Thread(task).start();
    }

    @FXML
    private void exportToPDF() {
        if (currentProduct == null || productDetails.isEmpty()) {
            showAlert("No Data", "Please load a product comparison first", Alert.AlertType.WARNING);
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.setInitialDirectory(exportService.getDefaultExportDirectory());
        fileChooser.setInitialFileName(exportService.generateFileName(currentProduct.getName(), "pdf"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(exportPdfButton.getScene().getWindow());
        if (file != null) {
            setStatus("Exporting to PDF...");
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return exportService.exportToPDF(productDetails, currentProduct.getName(), file);
                }
            };
            task.setOnSucceeded(event -> {
                if (task.getValue()) {
                    showAlert("Success", "PDF exported successfully!", Alert.AlertType.INFORMATION);
                    setStatus("PDF exported");
                } else {
                    showAlert("Error", "Failed to export PDF", Alert.AlertType.ERROR);
                    setStatus("Export failed");
                }
            });
            new Thread(task).start();
        }
    }

    @FXML
    private void exportToExcel() {
        if (currentProduct == null || productDetails.isEmpty()) {
            showAlert("No Data", "Please load a product comparison first", Alert.AlertType.WARNING);
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel");
        fileChooser.setInitialDirectory(exportService.getDefaultExportDirectory());
        fileChooser.setInitialFileName(exportService.generateFileName(currentProduct.getName(), "xlsx"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(exportExcelButton.getScene().getWindow());
        if (file != null) {
            setStatus("Exporting to Excel...");
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return exportService.exportToExcel(productDetails, currentProduct.getName(), file);
                }
            };
            task.setOnSucceeded(event -> {
                if (task.getValue()) {
                    showAlert("Success", "Excel exported successfully!", Alert.AlertType.INFORMATION);
                    setStatus("Excel exported");
                } else {
                    showAlert("Error", "Failed to export Excel", Alert.AlertType.ERROR);
                    setStatus("Export failed");
                }
            });
            new Thread(task).start();
        }
    }

    @FXML
    private void refreshData() {
        if (currentProduct == null) {
            autoRefreshService.triggerRefresh();
            setStatus("Refreshing all products...");
            return;
        }
        setStatus("Refreshing product data...");
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                productService.refreshProduct(currentProduct.getId());
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            loadProductComparison();
            setStatus("Data refreshed");
        });
        task.setOnFailed(event -> {
            showAlert("Error", "Refresh failed: " + task.getException().getMessage(), Alert.AlertType.ERROR);
            setStatus("Refresh failed");
        });
        new Thread(task).start();
    }
    @FXML
    private void toggleTheme() {
        ThemeManager.toggleTheme(themeToggle.getScene());
        String currentTheme = ThemeManager.getCurrentTheme();
        themeToggle.setText(currentTheme.equals("dark") ? "🌙" : "☀");
    }
    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showAlert("Error", "Could not open URL: " + url, Alert.AlertType.ERROR);
        }
    }
    private void setStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    public void shutdown() {
        autoRefreshService.stop();
    }
}