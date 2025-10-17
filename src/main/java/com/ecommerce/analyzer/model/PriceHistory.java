package com.ecommerce.analyzer.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Price History Model
 * Tracks historical prices for trend analysis
 */
public class PriceHistory {
    private final LongProperty id;
    private final LongProperty productDetailId;
    private final DoubleProperty price;
    private final ObjectProperty<LocalDateTime> recordedAt;

    public PriceHistory() {
        this.id = new SimpleLongProperty();
        this.productDetailId = new SimpleLongProperty();
        this.price = new SimpleDoubleProperty();
        this.recordedAt = new SimpleObjectProperty<>();
    }

    public PriceHistory(Long id, Long productDetailId, Double price, LocalDateTime recordedAt) {
        this();
        setId(id);
        setProductDetailId(productDetailId);
        setPrice(price);
        setRecordedAt(recordedAt);
    }

    // Property getters
    public LongProperty idProperty() { return id; }
    public LongProperty productDetailIdProperty() { return productDetailId; }
    public DoubleProperty priceProperty() { return price; }
    public ObjectProperty<LocalDateTime> recordedAtProperty() { return recordedAt; }

    // Getters and Setters
    public Long getId() { return id.get(); }
    public void setId(Long value) { id.set(value); }

    public Long getProductDetailId() { return productDetailId.get(); }
    public void setProductDetailId(Long value) { productDetailId.set(value); }

    public Double getPrice() { return price.get(); }
    public void setPrice(Double value) { price.set(value); }

    public LocalDateTime getRecordedAt() { return recordedAt.get(); }
    public void setRecordedAt(LocalDateTime value) { recordedAt.set(value); }

    @Override
    public String toString() {
        return "PriceHistory{" +
                "id=" + getId() +
                ", price=" + getPrice() +
                ", recordedAt=" + getRecordedAt() +
                '}';
    }
}