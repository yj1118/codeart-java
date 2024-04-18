package apros.codeart.util;

public final class Common {

	private Common() {
	}

	public static boolean isNull(Object obj) {
		if (obj == null)
			return true;

		return (obj instanceof INullProxy) && ((INullProxy) obj).isNull();
	}

}