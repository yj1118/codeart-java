package apros.codeart.dto;

import static apros.codeart.runtime.TypeUtil.as;
import static apros.codeart.runtime.TypeUtil.is;
import static apros.codeart.util.StringUtil.last;
import static apros.codeart.util.StringUtil.removeLast;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import apros.codeart.dto.serialization.IDTOValue;
import apros.codeart.runtime.EnumUtil;
import com.google.common.primitives.*;

import apros.codeart.pooling.util.StringPool;
import apros.codeart.runtime.FieldUtil;
import apros.codeart.util.Common;
import apros.codeart.util.ISO8601;
import apros.codeart.util.StringUtil;

public class JSON {
    private JSON() {
    }

    public static String getCode(Object value) {
        return StringPool.using((sb) -> {
            writeValue(sb, value);
        });
    }

    public static void writeValue(StringBuilder sb, Object value) {

        var dtoValue = as(value, IDTOValue.class);
        if (dtoValue != null) {
            dtoValue.writeValue(sb);
            return;
        }

        if (Common.isNull(value)) {
            sb.append("null");
            return;
        }

        var valueClass = value.getClass();

        if (valueClass == String.class || valueClass == UUID.class) {
            writeString(sb, value.toString());
            return;
        }

        if (valueClass == Long.class ||
                valueClass == Integer.class ||
                valueClass == Float.class ||
                valueClass == Byte.class ||
                valueClass == Double.class ||
                valueClass == Short.class) {
            sb.append(value);
            return;
        }

        if (valueClass == Boolean.class) {
            sb.append(value.toString().toLowerCase());
            return;
        }

        if (valueClass.isEnum()) {
            var ordinalValue = EnumUtil.getValue(value);
            sb.append(ordinalValue);
            return;
        }

        if (valueClass == Instant.class) {
            writeInstant(sb, (Instant) value);
            return;
        }

        if (valueClass == LocalDateTime.class) {
            writeLocalDateTime(sb, (LocalDateTime) value);
            return;
        }

        if (valueClass == ZonedDateTime.class) {
            writeZonedDateTime(sb, (ZonedDateTime) value);
            return;
        }

        if (valueClass == BigDecimal.class) {
            writeBigDecimal(sb, (BigDecimal) value);
            return;
        }

        if (is(valueClass, Map.class)) {
            writeMap(sb, (Map<?, ?>) value);
            return;
        }

        if (is(valueClass, Iterable.class)) {
            writeIterable(sb, (Iterable<?>) value);
            return;
        } else {
            writeObject(sb, value);
        }
    }

    private static void writeBigDecimal(StringBuilder sb, BigDecimal value) {
        sb.append("\"");
        sb.append(value.toString());
        sb.append("\"");
    }

    private static void writeZonedDateTime(StringBuilder sb, ZonedDateTime value) {
        sb.append("\"");
        sb.append(ISO8601.toString(value));
        sb.append("\"");
    }

    private static void writeLocalDateTime(StringBuilder sb, LocalDateTime value) {
        sb.append("\"");
        sb.append(ISO8601.toString(value));
        sb.append("\"");
    }

    private static void writeInstant(StringBuilder sb, Instant value) {
        sb.append("\"");
        sb.append(ISO8601.toString(value));
        sb.append("\"");
    }

    // 这主要是将带换行符的json代码，每行末尾添加了\字符，这样js引擎才能识别带换行的字符串
    public static void writeString(StringBuilder sb, String value) {

        sb.append("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\': {
                    sb.append("\\\\");
                    break;
                }
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        sb.append("\"");
    }

    private static void writeMap(StringBuilder sb, Map<?, ?> value) {
        boolean hasItems = false;
        sb.append("{");
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            writeValue(sb, entry.getKey());
            sb.append(":");
            writeValue(sb, entry.getValue());
            sb.append(",");
            if (hasItems == false)
                hasItems = true;
        }
        if (hasItems)
            removeLast(sb);
        sb.append("}");
    }

    private static void writeIterable(StringBuilder sb, Iterable<?> value) {
        boolean hasItems = false;
        sb.append("[");
        for (var item : value) {
            writeValue(sb, item);
            sb.append(",");
            if (hasItems == false)
                hasItems = true;
        }

        if (hasItems)
            removeLast(sb);
        sb.append("]");
    }

    private static void writeObject(StringBuilder sb, Object value) {
        var dto = as(value, DTObject.class);
        if (dto != null) {
            dto.fillCode(sb, false, true);
            return;
        }

        sb.append("{");

        FieldUtil.each(value, (n, v) -> {
            sb.append(n);
            sb.append(":");
            writeValue(sb, v);
            sb.append(",");
        });

        if (last(sb) == ',')
            removeLast(sb);
        sb.append("}");

    }

    public static String readString(String value) {
        if (StringUtil.isNullOrEmpty(value))
            return StringUtil.empty();

        var sb = new StringBuilder();
        char sign = StringUtil.charEmpty();
        for (var pos = 0; pos < value.length(); pos++) {
            var c = value.charAt(pos);
            switch (c) {
                case '\'':
                case '\"': {
                    if (sign == StringUtil.charEmpty()) {
                        sign = c;
                    } else {
                        if (sign == c) {
                            sign = StringUtil.charEmpty();
                        } else {
                            sb.append(c);
                        }
                    }
                }
                break;
                case '\\': {
                    if (pos == value.length() - 1) {
                        // 最后个字符
                        sb.append(c);
                    } else {
                        var next = value.charAt(pos + 1);

                        switch (next) {
                            case '\"': {
                                sb.append('\"');
                                pos++;
                            }
                            break;
                            case '\\': {
                                sb.append('\\');
                                pos++;
                            }
                            break;
                            case 'b': {
                                sb.append('\b');
                                pos++;
                            }
                            break;
                            case 'f': {
                                sb.append('\f');
                                pos++;
                            }
                            break;
                            case 'n': {
                                sb.append('\n');
                                pos++;
                            }
                            break;
                            case 'r': {
                                sb.append('\r');
                                pos++;
                            }
                            break;
                            case 't': {
                                sb.append('\t');
                                pos++;
                            }
                            break;
                        }
                    }

                    break;
                }
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static Object getValueByString(String code) {
        var value = JSON.readString(code);
        // 注意，在没有显示指名用什么类型的日期时，默认给出的时间是Instant类型，表示一个UTC时间点，可以转换为任意别的时间
        var time = JSON.getInstant(value);
        if (time != null)
            return time;
        return value;
    }

    public static Object getValueByNotString(String code) {
        if (code == null || code.equals("null"))
            return null;
        else if (code.equalsIgnoreCase("true"))
            return true;
        else if (code.equalsIgnoreCase("false"))
            return false;
        else {
            var intValue = Ints.tryParse(code);
            if (intValue != null)
                return intValue;

            var longValue = Longs.tryParse(code);
            if (longValue != null)
                return longValue;

            var floatValue = Floats.tryParse(code);

            if (floatValue != null) {
                var dot = code.indexOf(".");
                if (dot == -1 || (code.length() - dot - 1) <= 6) {
                    // 6位及以内的小数采用float，因为超过6位，输出就不准确了，被截获了，float转string
                    return floatValue;
                }
            }

            var doubleValue = Doubles.tryParse(code);
            if (doubleValue != null)
                return doubleValue;
        }
        return code;
    }

    public static Byte getByteRef(String code) {
        return Byte.parseByte(code);
    }

    public static Long getLongRef(String code) {
        return Longs.tryParse(code);
    }

    public static Integer getIntRef(String code) {
        return Ints.tryParse(code);
    }

    public static Boolean getBooleanRef(String code) {
        if ("true".equalsIgnoreCase(code)) {
            return true;
        } else if ("false".equalsIgnoreCase(code)) {
            return false;
        }
        return null;
    }

    public static Float getFloatRef(String code) {
        return Floats.tryParse(code);
    }

    public static Double getDoubleRef(String code) {
        return Doubles.tryParse(code);
    }

    public static Short getShortRef(String code) {
        return Short.parseShort(code);
    }

    public static Character getCharRef(String code) {
        return code.charAt(0);
    }

    public static Instant getInstant(String code) {
        if (ISO8601.is(code)) {
            return Instant.parse(code);
        }
        return null;
    }
}
