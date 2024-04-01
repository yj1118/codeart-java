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
	public static void assertClean(MethodGenerator g, Supplier<String> error) {
		if (g.evalStack().size() > 0) {
			g.broken();
			throw new IllegalArgumentException(error.get());
		}
	}

	public static void assertClean(MethodGenerator g) {
		if (g.evalStack().size() > 0) {
			g.broken();
			throw new IllegalArgumentException(Language.strings("codeart", "StackNotEmpty"));
		}
	}

	public static void assertRefs(MethodGenerator g, int expectedCount) {
		try {
			g.evalStack().currentFrame().assertRefs(expectedCount);
		} catch (Exception ex) {
			g.broken();
			throw ex;
		}
	}

	/**
	 * 检查栈顶至少值有多少个
	 * 
	 * @param count
	 */
	public static void assertCount(MethodGenerator g, int expectedCount) {
		assertCount(g.evalStack(), expectedCount);
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
