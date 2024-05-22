package apros.codeart;

public class UIException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4503168230778998420L;

	public UIException() {
	}

	public UIException(String message) {
		super(message);
	}

	public UIException(String message, Exception innerException) {
		super(message, innerException);
	}

	public static boolean is(Exception ex) {
		return UIException.class.isAssignableFrom(ex.getClass());
	}

}
