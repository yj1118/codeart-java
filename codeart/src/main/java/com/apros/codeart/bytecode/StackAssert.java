package com.apros.codeart.bytecode;

import static com.apros.codeart.i18n.Language.strings;

final class StackAssert {
	private StackAssert() {
	}

	public static void isClean(EvaluationStack evalStack) {
		if (evalStack.frameSize() > 0)
			throw new IllegalArgumentException(strings("StackFramesCountError"));
	}

}
