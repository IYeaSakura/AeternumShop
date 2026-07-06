package net.sakurain.mc.shop.api;

import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.api.events.TradeListener;
import net.sakurain.mc.shop.currency.CurrencyManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShopAPI {

    private static final List<TradeListener> tradeListeners = new ArrayList<>();

    public static CurrencyManager getCurrencyManager() {
        return AeternumShop.getInstance().getCurrencyManager();
    }

    public static long getBalance(Player player) {
        return getCurrencyManager().getPlayerBalance(player);
    }

    public static boolean withdraw(Player player, long amount) {
        return getCurrencyManager().withdraw(player, amount);
    }

    public static long deposit(Player player, long amount) {
        return getCurrencyManager().deposit(player, amount);
    }

    public static void registerTradeListener(TradeListener listener) {
        tradeListeners.add(listener);
    }

    public static void unregisterTradeListener(TradeListener listener) {
        tradeListeners.remove(listener);
    }

    public static List<TradeListener> getTradeListeners() {
        return new ArrayList<>(tradeListeners);
    }
}
