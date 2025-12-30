package apros.codeart.ddd;

public class EmptyableFloat extends Emptyable<Float> {

    public EmptyableFloat(Float value) {
        super(value);
    }

    public static EmptyableFloat createEmpty() {
        return new EmptyableFloat(null);
    }

    public final static Class<?> ValueType = float.class;

    public final static EmptyableFloat Empty = new EmptyableFloat(null);
}