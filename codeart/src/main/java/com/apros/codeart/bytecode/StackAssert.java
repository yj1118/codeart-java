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

	public static void assertRefs(EvaluationStack evalStack, int expectedCount) {

		evalStack.currentFrame().assertRefs(expectedCount);
	}

	/**
	 * 检查栈顶至少值有多少个
	 * 
	 * @param count
	 */
	public static void assertCount(EvaluationStack evalStack, int expectedCount) {
		evalStack.currentFrame().assertCount(expectedCount);
	}

}
