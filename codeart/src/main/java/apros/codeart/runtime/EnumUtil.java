package apros.codeart.runtime;

import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.util.function.Function;

import apros.codeart.util.LazyIndexer;

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

    private static Function<Class<?>, Method> _resolveGetValue = LazyIndexer.init((enumType) -> {
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
