package apros.codeart.ddd.saga;

public class RemoteEventFailedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8178318978891235626L;

	public RemoteEventFailedException(String message) {
		super(message);
	}
}
