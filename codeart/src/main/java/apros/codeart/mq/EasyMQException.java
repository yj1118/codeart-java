package apros.codeart.mq;

public class EasyMQException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7200186499852553006L;

	public EasyMQException() {
	}

	public EasyMQException(String message) {
		super(message);
	}

	public EasyMQException(String message, Exception innerException) {
		super(message, innerException);
	}

}
