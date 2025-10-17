package com.ecommerce.analyzer.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Product Model
 * Represents a product in the system
 */
public class Product {
    private final LongProperty id;
    private final StringProperty name;
    private final StringProperty productUrl;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> lastUpdated;

    public Product() {
        this.id = new SimpleLongProperty();
        this.name = new SimpleStringProperty();
        this.productUrl = new SimpleStringProperty();
        this.createdAt = new SimpleObjectProperty<>();
        this.lastUpdated = new SimpleObjectProperty<>();
    }

    public Product(Long id, String name, String productUrl,
                   LocalDateTime createdAt, LocalDateTime lastUpdated) {
        this();
        setId(id);
        setName(name);
        setProductUrl(productUrl);
        setCreatedAt(createdAt);
        setLastUpdated(lastUpdated);
    }

    // Property getters for JavaFX binding
    public LongProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty productUrlProperty() { return productUrl; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public ObjectProperty<LocalDateTime> lastUpdatedProperty() { return lastUpdated; }

    // Getters and Setters
    public Long getId() { return id.get(); }
    public void setId(Long value) { id.set(value); }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }

    public String getProductUrl() { return productUrl.get(); }
    public void setProductUrl(String value) { productUrl.set(value); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }

    public LocalDateTime getLastUpdated() { return lastUpdated.get(); }
    public void setLastUpdated(LocalDateTime value) { lastUpdated.set(value); }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", productUrl='" + getProductUrl() + '\'' +
                '}';
    }
}