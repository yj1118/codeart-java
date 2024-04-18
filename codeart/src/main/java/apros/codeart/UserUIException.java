package apros.codeart;

import apros.codeart.runtime.TypeUtil;

public class UserUIException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4503168230778998420L;

	public UserUIException() {
	}

	public UserUIException(String message) {
		super(message);
	}

	public UserUIException(String message, Exception innerException) {
		super(message, innerException);
	}

	public static boolean isUserUIException(Exception ex) {
		Throwable temp = ex;
		while (temp != null) {
			if (TypeUtil.is(temp.getClass(), UserUIException.class))
				return true;
			temp = temp.getCause();
		}
		return false;
	}

}
