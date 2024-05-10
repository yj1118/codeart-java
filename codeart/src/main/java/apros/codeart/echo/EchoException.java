package apros.codeart.echo;

public class EchoException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7200186499852553006L;

	public EchoException() {
	}

	public EchoException(String message) {
		super(message);
	}

	public EchoException(String message, Exception innerException) {
		super(message, innerException);
	}

}
