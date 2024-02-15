package apros.codeart.container;

/**
 * 会话容器
 * 
 * 该容器返回的对象在一个会话中单例
 */
public final class SessionContainer {
	private SessionContainer() {
	}

	public static <T> T getInstance() {
		return null;
	}
}