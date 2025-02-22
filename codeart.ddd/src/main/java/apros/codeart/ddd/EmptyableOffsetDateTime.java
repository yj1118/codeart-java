package apros.codeart.ddd;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public class EmptyableOffsetDateTime extends Emptyable<OffsetDateTime> {

    public EmptyableOffsetDateTime(OffsetDateTime value) {
        super(value);
    }

    public static EmptyableOffsetDateTime createEmpty() {
        return new EmptyableOffsetDateTime(null);
    }

    public final static Class<?> ValueType = OffsetDateTime.class;

    public final static EmptyableOffsetDateTime Empty = new EmptyableOffsetDateTime(null);


    public static EmptyableOffsetDateTime now() {
        return new EmptyableOffsetDateTime(OffsetDateTime.now());
    }


}
