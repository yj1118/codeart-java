package apros.codeart.ddd;

import java.time.LocalDateTime;

/**
 * 不带时区的可为空的时间类型
 */
public class EmptyableDateTime extends Emptyable<LocalDateTime> {

	public EmptyableDateTime(LocalDateTime value) {
		super(value);
	}

	public static EmptyableDateTime createEmpty() {
		return new EmptyableDateTime(null);
	}

	public final static Class<?> ValueType = LocalDateTime.class;

	public final static EmptyableDateTime Empty = new EmptyableDateTime(null);
	
	
	public static EmptyableDateTime now(){
		return new EmptyableDateTime(LocalDateTime.now());
	}
	
	
}
