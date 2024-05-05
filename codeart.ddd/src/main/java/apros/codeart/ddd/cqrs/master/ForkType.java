package apros.codeart.ddd.cqrs.master;

public enum ForkType {
	/**
	 * 基于数据库的复刻
	 */
	DB((byte) 1);

	private final byte _value;

	// 构造函数
	ForkType(byte value) {
		this._value = value;
	}

	public byte getValue() {
		return _value;
	}
}
