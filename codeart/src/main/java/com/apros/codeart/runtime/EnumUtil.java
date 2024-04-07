package com.apros.codeart.runtime;

public final class EnumUtil {

	private EnumUtil() {
	}

	/*
	 * 
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

	/**
	 * 获取枚举的基类型，注意，枚举需要满足ca规范
	 * 
	 * @return
	 */
	public static Class<?> getUnderlyingType() {
		return byte.class;
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
