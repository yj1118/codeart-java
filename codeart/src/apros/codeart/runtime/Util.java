package apros.codeart.runtime;

public class Util {
	@SuppressWarnings("unchecked")
	public static <T> T as(Object obj, Class<T> cls) {
		if (cls.isInstance(obj))
			return (T) obj;
		return null;
	}

	public static boolean is(Object obj, Class<?> cls) {
		return cls.isInstance(obj);
	}

	public static boolean any(Object obj, Class<?>... clses) {
		for (var cls : clses) {
			if (cls.isInstance(obj))
				return true;
		}
		return false;
	}

}
