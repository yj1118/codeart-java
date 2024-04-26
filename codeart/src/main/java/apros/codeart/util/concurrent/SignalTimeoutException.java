package apros.codeart.util.concurrent;

import apros.codeart.util.StringUtil;

public class SignalTimeoutException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8836006258796822944L;

	public SignalTimeoutException() {
		super(StringUtil.empty());
	}

}
