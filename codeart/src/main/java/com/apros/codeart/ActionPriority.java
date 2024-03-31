package com.apros.codeart;

public enum ActionPriority {

	/**
	 * 低优先级
	 */
	Low((byte)1),
	/// <summary>
	/// 用户使用的中等优先级
	/// </summary>
	User((byte)2),
	/// <summary>
	/// 高优先级
	/// </summary>
	High((byte)3);

	private final byte _value;

	// 构造函数
	ActionPriority(byte value) {
		this._value = value;
	}

	// 获取枚举值关联的数字
	public byte getValue() {
		return _value;
	}

}
