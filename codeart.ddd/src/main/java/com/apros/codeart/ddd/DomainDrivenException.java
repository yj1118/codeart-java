package com.apros.codeart.ddd;

public class DomainDrivenException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DomainDrivenException(String message) {
		super(message);
	}
}
