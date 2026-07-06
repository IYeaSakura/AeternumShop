package net.sakurain.mc.shop.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class MailboxEntry {

    private int id;
    private UUID playerUuid;
    private MailboxType type;
    private String itemType;
    private int itemAmount;
    private long currencyAmount;
    private boolean received;
    private LocalDateTime createTime;

    public MailboxEntry() {
    }

    public MailboxEntry(UUID playerUuid, MailboxType type, String itemType, int itemAmount,
                        long currencyAmount, boolean received, LocalDateTime createTime) {
        this.playerUuid = playerUuid;
        this.type = type;
        this.itemType = itemType;
        this.itemAmount = itemAmount;
        this.currencyAmount = currencyAmount;
        this.received = received;
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public MailboxType getType() {
        return type;
    }

    public void setType(MailboxType type) {
        this.type = type;
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

    public long getCurrencyAmount() {
        return currencyAmount;
    }

    public void setCurrencyAmount(long currencyAmount) {
        this.currencyAmount = currencyAmount;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
