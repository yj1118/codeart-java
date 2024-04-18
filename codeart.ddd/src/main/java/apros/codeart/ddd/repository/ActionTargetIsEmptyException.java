package apros.codeart.ddd.repository;

import apros.codeart.ddd.DomainDrivenException;

public class ActionTargetIsEmptyException extends DomainDrivenException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8869952816967040573L;

	public ActionTargetIsEmptyException(String message) {
		super(message);
	}
}
