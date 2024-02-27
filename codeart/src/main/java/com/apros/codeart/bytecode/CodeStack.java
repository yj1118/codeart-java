package com.apros.codeart.bytecode;

import java.util.LinkedList;

final class ScopeStack {

	private LinkedList<CodeScope> _scopes;

	private CodeScope _current;

	public ScopeStack() {
		_scopes = new LinkedList<CodeScope>();
	}
}