package net.sakurain.mc.shop.api.events;

import org.bukkit.entity.Player;

public interface TradeListener {

    void onPlayerTrade(Player buyer, Player seller, String itemType, int amount, long price);

    void onSystemBuy(Player seller, String itemType, int amount, long reward);

    void onSystemSell(Player buyer, String itemType, int amount, long cost);
}
