package apros.codeart.util;

/**
 * 代表事件中的空参数
 */
public final class EmptyEventArgs {
	private EmptyEventArgs() {
	}

	public static final EmptyEventArgs Instance = new EmptyEventArgs();
}
