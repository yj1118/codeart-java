package apros.codeart.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public final class ISO8601 {

	private ISO8601() {
	}

	/**
	 * Pattern是线程安全的
	 */
	private final static Pattern ISO8601 = Pattern.compile(
			"^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})?$", Pattern.CASE_INSENSITIVE);

	public static boolean is(String code) {
		return ISO8601.matcher(code).matches();
	}

	// 预编译正则表达式
	private static final Pattern TIMEZONE_PATTERN = Pattern.compile(".*(?:Z|[+-]\\d{2}:\\d{2})$");

	public static boolean hasTimeZone(String iso8601String) {
		return TIMEZONE_PATTERN.matcher(iso8601String).matches();
	}

	/**
	 * @param iso8601String 基于iso8601的时间字符串，例如："2024-03-06T15:45:30.123Z"和"1984-11-18T02:30:50";
	 * @return
	 */
	public static LocalDateTime getLocalDateTime(String iso8601String) {

		if (hasTimeZone(iso8601String)) {
			// 将字符串解析为OffsetDateTime
			OffsetDateTime odt = OffsetDateTime.parse(iso8601String);
			// 从OffsetDateTime获取LocalDateTime
			return odt.toLocalDateTime();
		}

		return LocalDateTime.parse(iso8601String);
	}

	public static String toString(LocalDateTime value) {
		// 转换为ZonedDateTime，这里以系统默认时区为例
		ZonedDateTime zonedDateTime = value.atZone(ZoneId.systemDefault());
		return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	/**
	 * 
	 * Instant代表的是一个时间点，以自1970年1月1日UTC（协调世界时间）以来的秒数和纳秒数来定义。
	 * 
	 * 尽管Instant本身不包含时区信息，但它是基于UTC时间的。这意味着Instant可以直接从遵循
	 * 
	 * ISO 8601标准的字符串中解析得到，因为ISO 8601格式提供了一个与时区无关的、全球统一的时间表示方法。
	 * 
	 * @param iso8601String
	 * @return
	 */
	public static Instant getInstant(String iso8601String) {
		return Instant.parse(iso8601String);
	}

	public static String toString(Instant value) {
		// 调用Instant 的 toString()方法会返回这个时间点的ISO 8601格式字符串，如"2024-03-06T15:45:30.123Z"。
		return value.toString();
	}

	/**
	 * 从标准的iso8601字符串中，获得带时区的时间
	 * 
	 * @param iso8601String
	 * @return
	 */
	public static ZonedDateTime getZonedDateTime(String iso8601String) {
		return ZonedDateTime.parse(iso8601String);
	}

	/**
	 * 获得字符串格式的时差信息
	 * 
	 * @return
	 */
	public static String getSystemZoneOffset() {
		return getZoneOffset(ZoneId.systemDefault());
	}

	/**
	 * 获得字符串格式的时差信息
	 * 
	 * @param zoneId
	 * @return
	 */
	public static String getZoneOffset(ZoneId zoneId) {

		// 获取当前时间点的Instant
		Instant instant = Instant.now();

		// 从ZoneId获取ZoneOffset
		ZoneOffset zoneOffset = zoneId.getRules().getOffset(instant);
		return zoneOffset.toString();
	}

}
