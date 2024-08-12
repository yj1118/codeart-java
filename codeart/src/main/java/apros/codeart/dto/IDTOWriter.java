package apros.codeart.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public interface IDTOWriter {

    void writeByte(String name, byte value);

    void writeShort(String name, short value);

    void writeInt(String name, int value);

    void writeLong(String name, long value);

    void writeFloat(String name, float value);

    void writeDouble(String name, double value);

    void writeChar(String name, char value);

    void writeBoolean(String name, boolean value);

    void writeLocalDateTime(String name, LocalDateTime value);

    void writeInstant(String name, Instant value);

    void writeString(String name, String value);

    void writeObject(String name, Object value);

    void writeValue(String name, Object value);

    void writeElement(String name, Object element);

    void writeElementByte(String name, byte value);

    void writeElementShort(String name, short value);

    void writeElementInt(String name, int value);

    void writeElementLong(String name, long value);

    void writeElementFloat(String name, float value);

    void writeElementDouble(String name, double value);

    void writeElementChar(String name, char value);

    void writeElementBoolean(String name, boolean value);

    /// <summary>
    /// 写入一个空数组
    /// </summary>
    /// <param name="name"></param>
    void writeArray(String name);

//	<T extends Enum<T>> void writeEnum(String name, T enumValue);

}
//
//
//public class EnumReader {
//
//    // 泛型方法，接受枚举类的 Class 对象和枚举常量的名称作为参数，返回对应的枚举常量
//    public static <T extends Enum<T>> T readEnum(Class<T> enumClass, String enumName) {
//        // 使用 Enum 类的 valueOf() 方法来获取枚举常量
//        return Enum.valueOf(enumClass, enumName);
//    }
//
//    public static void main(String[] args) {
//        // 示例：调用 readEnum 方法并传入枚举类和枚举常量的名称
//        Day day = readEnum(Day.class, "MONDAY");
//        System.out.println("Day: " + day);
//
//        Month month = readEnum(Month.class, "JANUARY");
//        System.out.println("Month: " + month);
//    }
//
//    // 示例枚举类 Day 和 Month
//    enum Day {
//        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
//    }
//
//    enum Month {
//        JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
//    }
//}

//
//public class EnumReader {
//
//    // 泛型方法，接受枚举类的 Class 对象和枚举常量的序号作为参数，返回对应的枚举常量
//    public static <T extends Enum<T>> T readEnumByOrdinal(Class<T> enumClass, int ordinal) {
//        // 使用 values() 方法获取枚举类中的所有枚举常量
//        T[] enumConstants = enumClass.getEnumConstants();
//        
//        // 根据序号获取对应的枚举常量
//        if (ordinal >= 0 && ordinal < enumConstants.length) {
//            return enumConstants[ordinal];
//        } else {
//            throw new IllegalArgumentException("Invalid ordinal value");
//        }
//    }
//
//    public static void main(String[] args) {
//        // 示例：调用 readEnumByOrdinal 方法并传入枚举类和序号
//        Day day = readEnumByOrdinal(Day.class, 0);
//        System.out.println("Day: " + day);
//
//        Month month = readEnumByOrdinal(Month.class, 1);
//        System.out.println("Month: " + month);
//    }
//
//    // 示例枚举类 Day 和 Month
//    enum Day {
//        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
//    }
//
//    enum Month {
//        JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
//    }
//}
