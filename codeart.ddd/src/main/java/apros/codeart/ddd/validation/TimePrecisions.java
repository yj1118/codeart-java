package apros.codeart.ddd.validation;

public enum TimePrecisions {
	/**
	 * 秒
	 */
	Second((byte) 1),

	/**
	 * 100毫秒
	 */
	Millisecond100((byte) 2),

	/**
	 * 10毫秒
	 */
	Millisecond10((byte) 3),

	/**
	 * 毫秒
	 */
	Millisecond((byte) 4),

	/**
	 * 100微秒
	 */
	Microsecond100((byte) 5),

	/**
	 * 10微秒
	 */
	Microsecond10((byte) 6),

	/**
	 * 微秒
	 */
	Microsecond((byte) 7),

	/**
	 * 100纳秒
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
