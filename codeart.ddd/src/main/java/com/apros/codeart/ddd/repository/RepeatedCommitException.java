package com.apros.codeart.ddd.repository;

import com.apros.codeart.ddd.DomainDrivenException;
import com.apros.codeart.i18n.Language;

public class RepeatedCommitException extends DomainDrivenException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7366068880543324255L;

	public RepeatedCommitException() {
		super(Language.strings("RepeatedCommit"));
	}
}
