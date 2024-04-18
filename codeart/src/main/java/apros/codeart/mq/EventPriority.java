package apros.codeart.mq;

public enum EventPriority {
	/**
	 * 低优先级
	 */
	Low((byte) 1),

	/**
	 * 中等优先级
	 */
	Medium((byte) 2),

	/**
	 * 高优先级
	 */
	High((byte) 3);

	private final byte _value;

	// 构造函数
	EventPriority(byte value) {
		this._value = value;
	}

	// 获取枚举值关联的数字
	public byte getValue() {
		return _value;
	}
}
