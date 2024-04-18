package apros.codeart.util;

import java.util.function.Consumer;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public final class HashUtil {
	private HashUtil() {
	}

	// HashFunction 是线程安全的
	private static HashFunction _hash32 = Hashing.goodFastHash(32);

	/**
	 * hash32提供了一个相对快速但不是加密安全的哈希函数，适用于一般用途
	 * 
	 * @param action
	 * @return
	 */
	public static int hash32(Consumer<HashCodeBuilder> action) {
		var hasher = _hash32.newHasher();
		var wrapper = new HashCodeBuilder(hasher);
		action.accept(wrapper);
		return wrapper.toHashCode();
	}

	public static class HashCodeBuilder {

		private Hasher _haser;

		private HashCodeBuilder(Hasher haser) {
			_haser = haser;
		}

		public void append(String value) {
			_haser.putString(value, java.nio.charset.StandardCharsets.UTF_8);
		}

		public void append(Object value) {
			_haser.putInt(value.hashCode());
		}

		public int toHashCode() {
			return _haser.hash().asInt();
		}

	}

}
