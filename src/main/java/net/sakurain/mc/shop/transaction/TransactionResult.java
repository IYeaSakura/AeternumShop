package net.sakurain.mc.shop.transaction;

public class TransactionResult {

    private final boolean success;
    private final String messageKey;
    private final String[] placeholders;

    public TransactionResult(boolean success, String messageKey, String... placeholders) {
        this.success = success;
        this.messageKey = messageKey;
        this.placeholders = placeholders;
    }

    public static TransactionResult success(String messageKey, String... placeholders) {
        return new TransactionResult(true, messageKey, placeholders);
    }

    public static TransactionResult fail(String messageKey, String... placeholders) {
        return new TransactionResult(false, messageKey, placeholders);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String[] getPlaceholders() {
        return placeholders;
    }
}
