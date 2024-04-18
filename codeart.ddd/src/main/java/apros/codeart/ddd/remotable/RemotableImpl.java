package apros.codeart.ddd.remotable;

public final class RemotableImpl {
	private RemotableImpl() {
	}

	public static boolean has(Class<?> objectType) {
		return objectType.getAnnotation(Remotable.class) != null;
	}

}
