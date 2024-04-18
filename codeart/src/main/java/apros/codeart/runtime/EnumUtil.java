package apros.codeart.runtime;

import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.util.function.Function;

import apros.codeart.util.LazyIndexer;

/**
 * ca的枚举规范:
 * 
 * 1.值类型为byte
 * 
 * public enum Status {
 * 
 * NEW((byte)1), IN_PROGRESS((byte)2), COMPLETED((byte)3), CANCELLED((byte)4);
 * 
 * private final byte value;
 * 
 * private Status(byte value) { this.value = value; }
 * 
 * public byte getValue() { return value; } }
 */
public final class EnumUtil {

	private EnumUtil() {
	}

	/**
	 * 获取枚举的基类型，注意，枚举需要满足ca规范
	 * 
	 * 
	 * 
	 * @return
	 */
	public static Class<?> getUnderlyingType() {
		return byte.class;
	}

	private static Function<Class<?>, Method> _resolveGetValue = LazyIndexer.init((enumType) -> {
		return MethodUtil.resolveSlim(enumType, "getValue");
	});

	public static Object getValue(Object enumValue) {

		try {
			var getValeu = _resolveGetValue.apply(enumValue.getClass());
			return getValeu.invoke(enumValue);
		} catch (Exception e) {
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
