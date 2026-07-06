package net.sakurain.mc.shop.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String format(LocalDateTime time) {
        if (time == null) return "";
        return time.format(FORMATTER);
    }

    public static String formatRemaining(LocalDateTime expireTime) {
        if (expireTime == null) return "未知";
        long seconds = Duration.between(LocalDateTime.now(), expireTime).getSeconds();
        if (seconds <= 0) return "已过期";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d小时 %d分钟", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, secs);
        } else {
            return String.format("%d秒", secs);
        }
    }
}
