package apros.codeart.util;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public final class HashUtil {
	private HashUtil() {
	}

	// HashFunction 是线程安全的
	private static final HashFunction _hash32 = Hashing.goodFastHash(32);

	private static final HashFunction _hash64 = Hashing.goodFastHash(64);

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

	public static int hash64(Consumer<HashCodeBuilder> action) {
		var hasher = _hash64.newHasher();
		var wrapper = new HashCodeBuilder(hasher);
		action.accept(wrapper);
		return wrapper.toHashCode();
	}

	public static class HashCodeBuilder {

		private final Hasher _haser;

		private HashCodeBuilder(Hasher haser) {
			_haser = haser;
		}

		public void append(String value) {
			_haser.putString(value, java.nio.charset.StandardCharsets.UTF_8);
		}

		public void append(Object value) {
			putObjectToHasher(_haser,value);
		}

		public void append(Set<Map.Entry<String,Object>> map) {
			for (Map.Entry<String, Object> entry : map) {
				_haser.putString(entry.getKey(), java.nio.charset.StandardCharsets.UTF_8);
				putObjectToHasher(_haser, entry.getValue());
			}
		}

		private static void putObjectToHasher(Hasher hasher, Object value) {
			if (value instanceof Integer) {
				hasher.putInt((Integer) value);
			} else if (value instanceof Long) {
				hasher.putLong((Long) value);
			} else if (value instanceof Float) {
				hasher.putFloat((Float) value);
			} else if (value instanceof Double) {
				hasher.putDouble((Double) value);
			} else if (value instanceof Boolean) {
				hasher.putBoolean((Boolean) value);
			} else if (value instanceof String) {
				hasher.putString((String) value,  java.nio.charset.StandardCharsets.UTF_8);
			} else if (value != null) {
				hasher.putString(value.toString(),  java.nio.charset.StandardCharsets.UTF_8);
			}
		}

		public int toHashCode() {
			return _haser.hash().asInt();
		}

	}

}
