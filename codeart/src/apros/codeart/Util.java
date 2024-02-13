package apros.codeart;

public class Util {

	@SuppressWarnings("unchecked")
	public static <T> T as(Object obj, Class<T> cls) {
		if (cls.isInstance(obj))
			return (T) obj;
		return null;
	}

}