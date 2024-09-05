package apros.codeart.runtime;

import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;

/**
 * ca的枚举规范:
 * <p>
 * 1.值类型为byte
 * <p>
 * public enum Status {
 * <p>
 * NEW((byte)1), IN_PROGRESS((byte)2), COMPLETED((byte)3), CANCELLED((byte)4);
 * <p>
 * private final byte value;
 * <p>
 * private Status(byte value) { this.value = value; }
 * <p>
 * public byte getValue() { return value; } }
 */
public final class EnumUtil {

    private EnumUtil() {
    }

    /**
     * 获取枚举的基类型，注意，枚举需要满足ca规范
     *
     * @return
     */
    public static Class<?> getUnderlyingType() {
        return byte.class;
    }

    private static final Function<Class<?>, Method> _resolveGetValue = LazyIndexer.init((enumType) -> {
        return MethodUtil.resolve(enumType, "getValue", null);
    });

    public static Object getValue(Object enumValue) {
        try {
            var getValeu = _resolveGetValue.apply(enumValue.getClass());
            return getValeu.invoke(enumValue);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    //constant是值对应的枚举值
    private static record EnumValue(byte value, Object constant) {
    }

    private static final Function<Class<?>, List<EnumValue>> _enumValues = LazyIndexer.init((enumType) -> {
        var items = new ArrayList<EnumValue>();
        // 遍历枚举类中的所有枚举常量
        for (var enumConstant : enumType.getEnumConstants()) {
            try {
                // 获取枚举常量的 getValue 方法
                var enumValue = enumType.getMethod("getValue").invoke(enumConstant);

                items.add(new EnumValue((byte) enumValue, enumConstant));

            } catch (Throwable e) {
                throw propagate(e);
            }
        }

        return items;
    });

    private static byte toByte(Object value) {
        byte v = -1;

        if (value instanceof Number) {
            v = ((Number) value).byteValue();
        } else if (value instanceof String) {
            v = Byte.parseByte((String) value);
        }

        return v;
    }

    /**
     * 通用方法，根据byte值获取任意枚举类型的实例
     */
    public static Object fromValue(Class<?> enumType, Object value) {

        var v = toByte(value);

        var item = ListUtil.find(_enumValues.apply(enumType), (t) -> t.value() == v);

        if (item == null)
            throw new IllegalArgumentException("Unknown value: " + value + " for enum " + enumType.getName());

        return item.constant();
    }

//	try
//
//	{
//		// 获取枚举类的Class对象
//		Class<?> statusClass = Class.forName("Status");
//
//		// 获取所有枚举实例
//		Object[] enumConstants = statusClass.getEnumConstants();
//
//		// 遍历枚举实例
//		for (Object enumConstant : enumConstants) {
//			// 获取getValue方法
//			Method getValueMethod = statusClass.getDeclaredMethod("getValue");
//			// 调用getValue方法
//			int value = (Integer) getValueMethod.invoke(enumConstant);
//
//			System.out.println(enumConstant.toString() + " has value: " + value);
//		}
//	}catch(
//	Exception e)
//	{
//		e.printStackTrace();
//	}

}
