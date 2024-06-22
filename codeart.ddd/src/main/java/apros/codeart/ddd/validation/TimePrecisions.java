package apros.codeart.ddd.validation;

public enum TimePrecisions {
	/**
	 * 秒
	 */
	Second((byte) 1),

	/**
	 * 100毫秒（1位小数）
	 */
	Millisecond100((byte) 2),

	/**
	 * 10毫秒（2位小数）
	 */
	Millisecond10((byte) 3),

	/**
	 * 毫秒（3位小数）
	 */
	Millisecond((byte) 4),

	/**
	 * 100微秒（4位小数）
	 */
	Microsecond100((byte) 5),

	/**
	 * 10微秒（5位小数）
	 */
	Microsecond10((byte) 6),

	/**
	 * 微秒（6位小数）
	 */
	Microsecond((byte) 7),

	/**
	 * 100纳秒（7位小数）
	 */
	Nanosecond100((byte) 8);

	private final byte value;

	TimePrecisions(byte value) {
		this.value = value;
	}

	public byte getValue() {
		return this.value;
	}
}
