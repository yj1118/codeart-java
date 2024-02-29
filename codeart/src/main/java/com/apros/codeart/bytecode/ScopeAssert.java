package com.apros.codeart.bytecode;

import com.apros.codeart.i18n.Language;

class ScopeAssert {
	private ScopeAssert() {
	}

	/**
	 * 检查栈顶至少值有多少个
	 * 
	 * @param count
	 */
	public static void assertDepth(ScopeStack scopeStack, int expectedDepth) {
		if (scopeStack.getDepth() != expectedDepth)
			throw new IllegalArgumentException(Language.strings("ScopeDepthError"));
	}
}
