package net.sakurain.mc.shop.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class PlayerListing {

    private int id;
    private UUID seller;
    private String sellerName;
    private String itemType;
    private int itemAmount;
    private long price;
    private LocalDateTime postTime;
    private ListingStatus status;

    public PlayerListing() {
    }

    public PlayerListing(UUID seller, String sellerName, String itemType, int itemAmount, long price,
                         LocalDateTime postTime, ListingStatus status) {
        this.seller = seller;
        this.sellerName = sellerName;
        this.itemType = itemType;
        this.itemAmount = itemAmount;
        this.price = price;
        this.postTime = postTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getSeller() {
        return seller;
    }

    public void setSeller(UUID seller) {
        this.seller = seller;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public LocalDateTime getPostTime() {
        return postTime;
    }

    public void setPostTime(LocalDateTime postTime) {
        this.postTime = postTime;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }
}
