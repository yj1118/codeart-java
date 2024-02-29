package com.apros.codeart.bytecode;

import java.util.function.Supplier;

import com.apros.codeart.i18n.Language;

final class StackAssert {
	private StackAssert() {
	}

	/**
	 * 断言当前栈帧上没有值
	 * 
	 * @param evalStack
	 */
	public static void assertClean(EvaluationStack evalStack, Supplier<String> error) {
		if (evalStack.size() > 0)
			throw new IllegalArgumentException(error.get());
	}

	public static void assertClean(EvaluationStack evalStack) {
		if (evalStack.size() > 0)
			throw new IllegalArgumentException(Language.strings("StackNotEmpty"));
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
