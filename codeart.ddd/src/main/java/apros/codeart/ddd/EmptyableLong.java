package apros.codeart.ddd;

public class EmptyableLong extends Emptyable<Long> {

	public EmptyableLong(Long value) {
		super(value);
	}

	public static EmptyableLong createEmpty() {
		return new EmptyableLong(null);
	}

	public final static Class<?> ValueType = long.class;

	public final static EmptyableLong Empty = new EmptyableLong(null);
}