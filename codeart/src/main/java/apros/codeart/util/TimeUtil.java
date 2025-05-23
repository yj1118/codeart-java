package apros.codeart.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Function;

public final class TimeUtil {
    private TimeUtil() {
    }

    ;

    public static LocalDateTime toLocalDateTime(Date date) {
        // 将Date转换为Instant
        Instant instant = date.toInstant();

        // 将Instant转换为LocalDateTime，需要指定时区
        ZoneId zoneId = ZoneId.systemDefault(); // 使用系统默认时区
        return instant.atZone(zoneId).toLocalDateTime();
    }

    public static ZonedDateTime toUTC(Instant value) {
        return value.atZone(ZoneId.of("UTC"));
    }

    public static ZonedDateTime toUTC(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"));
    }

    public static String format(LocalDateTime value, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(value);
    }

    // DateTimeFormatter 是java的官方对象，承诺线程安全的
    private static final Function<String, DateTimeFormatter> _getDateTimeFormatter = LazyIndexer.init((pattern) -> {
        return DateTimeFormatter.ofPattern(pattern);
    });

    /**
     * format记忆化的版本，用空间换时间
     *
     * @param value
     * @param pattern
     * @return
     */
    public static String formatMemoized(Instant value, String pattern) {
        DateTimeFormatter formatter = _getDateTimeFormatter.apply(pattern);
        return formatter.format(value);
    }

    public static final ZonedDateTime MinZonedDateTime = ZonedDateTime.of(LocalDateTime.MIN, ZoneId.of("UTC"));
}