package com.ecommerce.analyzer.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Product Detail Model
 * Represents platform-specific product details
 */
public class ProductDetail {
    private final LongProperty id;
    private final LongProperty productId;
    private final StringProperty platform;
    private final DoubleProperty price;
    private final DoubleProperty rating;
    private final StringProperty seller;
    private final StringProperty deliveryTime;
    private final StringProperty returnPolicy;
    private final StringProperty warranty;
    private final StringProperty offers;
    private final StringProperty productLink;
    private final ObjectProperty<LocalDateTime> lastScraped;

    // NEW FIELDS
    private final IntegerProperty reviewCount;
    private final StringProperty availability;

    public ProductDetail() {
        this.id = new SimpleLongProperty();
        this.productId = new SimpleLongProperty();
        this.platform = new SimpleStringProperty();
        this.price = new SimpleDoubleProperty();
        this.rating = new SimpleDoubleProperty();
        this.seller = new SimpleStringProperty();
        this.deliveryTime = new SimpleStringProperty();
        this.returnPolicy = new SimpleStringProperty();
        this.warranty = new SimpleStringProperty();
        this.offers = new SimpleStringProperty();
        this.productLink = new SimpleStringProperty();
        this.lastScraped = new SimpleObjectProperty<>();

        // NEW PROPERTY INITIALIZATIONS
        this.reviewCount = new SimpleIntegerProperty();
        this.availability = new SimpleStringProperty();
    }

    public ProductDetail(Long id, Long productId, String platform, Double price,
                         Double rating, String seller, String deliveryTime,
                         String returnPolicy, String warranty, String offers,
                         String productLink, LocalDateTime lastScraped,
                         Integer reviewCount, String availability) {
        this();
        setId(id);
        setProductId(productId);
        setPlatform(platform);
        setPrice(price);
        setRating(rating);
        setSeller(seller);
        setDeliveryTime(deliveryTime);
        setReturnPolicy(returnPolicy);
        setWarranty(warranty);
        setOffers(offers);
        setProductLink(productLink);
        setLastScraped(lastScraped);
        setReviewCount(reviewCount);
        setAvailability(availability);
    }

    // Property getters for JavaFX binding
    public LongProperty idProperty() { return id; }
    public LongProperty productIdProperty() { return productId; }
    public StringProperty platformProperty() { return platform; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty ratingProperty() { return rating; }
    public StringProperty sellerProperty() { return seller; }
    public StringProperty deliveryTimeProperty() { return deliveryTime; }
    public StringProperty returnPolicyProperty() { return returnPolicy; }
    public StringProperty warrantyProperty() { return warranty; }
    public StringProperty offersProperty() { return offers; }
    public StringProperty productLinkProperty() { return productLink; }

    // NEW PROPERTY GETTERS
    public IntegerProperty reviewCountProperty() { return reviewCount; }
    public StringProperty availabilityProperty() { return availability; }

    // Getters and Setters
    public Long getId() { return id.get(); }
    public void setId(Long value) { id.set(value); }

    public Long getProductId() { return productId.get(); }
    public void setProductId(Long value) { productId.set(value); }

    public String getPlatform() { return platform.get(); }
    public void setPlatform(String value) { platform.set(value); }

    public Double getPrice() { return price.get(); }
    public void setPrice(Double value) { price.set(value); }

    public Double getRating() { return rating.get(); }
    public void setRating(Double value) { rating.set(value); }

    public String getSeller() { return seller.get(); }
    public void setSeller(String value) { seller.set(value); }

    public String getDeliveryTime() { return deliveryTime.get(); }
    public void setDeliveryTime(String value) { deliveryTime.set(value); }

    public String getReturnPolicy() { return returnPolicy.get(); }
    public void setReturnPolicy(String value) { returnPolicy.set(value); }

    public String getWarranty() { return warranty.get(); }
    public void setWarranty(String value) { warranty.set(value); }

    public String getOffers() { return offers.get(); }
    public void setOffers(String value) { offers.set(value); }

    public String getProductLink() { return productLink.get(); }
    public void setProductLink(String value) { productLink.set(value); }

    public LocalDateTime getLastScraped() { return lastScraped.get(); }
    public void setLastScraped(LocalDateTime value) { lastScraped.set(value); }

    // NEW GETTERS AND SETTERS
    public Integer getReviewCount() { return reviewCount.get(); }
    public void setReviewCount(Integer value) { reviewCount.set(value); }

    public String getAvailability() { return availability.get(); }
    public void setAvailability(String value) { availability.set(value); }

    @Override
    public String toString() {
        return "ProductDetail{" +
                "platform='" + getPlatform() + '\'' +
                ", price=" + getPrice() +
                ", rating=" + getRating() +
                ", reviewCount=" + getReviewCount() +
                ", availability='" + getAvailability() + '\'' +
                '}';
    }
}
