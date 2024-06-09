package apros.codeart.ddd;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class EmptyableZonedDateTime extends Emptyable<ZonedDateTime> {

	public EmptyableZonedDateTime(ZonedDateTime value) {
		super(value);
	}

	public static EmptyableZonedDateTime createEmpty() {
		return new EmptyableZonedDateTime(null);
	}

	public final static Class<?> ValueType = LocalDateTime.class;

	public final static EmptyableZonedDateTime Empty = new EmptyableZonedDateTime(null);
	
	
	public static EmptyableZonedDateTime now(){
		return new EmptyableZonedDateTime(ZonedDateTime.now());
	}
	
	
}
