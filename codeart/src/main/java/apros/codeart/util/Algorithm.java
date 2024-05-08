package apros.codeart.util;

public final class Algorithm {
	private Algorithm() {
	}

	/**
	 * 
	 * 将 {@code value} 扩大1.5倍
	 * 
	 * @param value
	 * @param max
	 * @return
	 */
	public static int increaseByOnePointFive(final int value, final int max) {
		// oldCapacity >> 1 是将 oldCapacity 右移一位的结果，相当于 oldCapacity
		// 的一半。将这个值加到原始容量上，得到的就是新容量，即原始容量的150%。
		// 在这里是将片段数为原数量的1.5倍
		if (value >= max)
			return max;
		int newValue = value + Math.max(1, value >> 1);
		return Math.min(newValue, max); // 确保不能超过maxCapacity
	}

	/**
	 * 
	 * 将 {@code value} 缩小1.5倍
	 * 
	 * @param value
	 * @param max
	 * @return
	 */
	public static int reduceByOnePointFive(final int value, final int min) {
		if (value <= min)
			return min;
		// 直接除，这种实现方式是高效的，因为除法操作在现代计算机中是非常快的。
		return Math.max(min, (int) ((float) value / 1.5F));
	}

}
