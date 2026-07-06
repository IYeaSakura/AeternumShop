package net.sakurain.mc.shop.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TradeRecord {

    private int id;
    private TradeType tradeType;
    private UUID buyer;
    private String buyerName;
    private UUID seller;
    private String sellerName;
    private String itemType;
    private int itemAmount;
    private long price;
    private LocalDateTime tradeTime;

    public TradeRecord() {
    }

    public TradeRecord(TradeType tradeType, UUID buyer, String buyerName, UUID seller, String sellerName,
                       String itemType, int itemAmount, long price, LocalDateTime tradeTime) {
        this.tradeType = tradeType;
        this.buyer = buyer;
        this.buyerName = buyerName;
        this.seller = seller;
        this.sellerName = sellerName;
        this.itemType = itemType;
        this.itemAmount = itemAmount;
        this.price = price;
        this.tradeTime = tradeTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    public UUID getBuyer() {
        return buyer;
    }

    public void setBuyer(UUID buyer) {
        this.buyer = buyer;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
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

    public LocalDateTime getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(LocalDateTime tradeTime) {
        this.tradeTime = tradeTime;
    }
}
