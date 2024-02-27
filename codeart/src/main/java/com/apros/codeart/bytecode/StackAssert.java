package com.apros.codeart.bytecode;

import java.util.function.Supplier;

final class StackAssert {
	private StackAssert() {
	}

	/**
	 * 断言当前栈帧上没有值
	 * 
	 * @param evalStack
	 */
	public static void isClean(EvaluationStack evalStack, Supplier<String> error) {
		if (evalStack.size() > 0)
			throw new IllegalArgumentException(error.get());
	}

}
