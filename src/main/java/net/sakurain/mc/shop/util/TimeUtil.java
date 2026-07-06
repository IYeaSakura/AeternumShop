package net.sakurain.mc.shop.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String format(LocalDateTime time) {
        if (time == null) return "";
        return time.format(FORMATTER);
    }
}
