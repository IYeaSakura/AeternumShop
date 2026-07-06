package net.sakurain.mc.shop.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class StringUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component toComponent(String text) {
        if (text == null) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
