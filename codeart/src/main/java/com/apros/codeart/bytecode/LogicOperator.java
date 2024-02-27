package com.apros.codeart.bytecode;

import java.util.function.BiConsumer;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public final class LogicOperator {

	private BiConsumer<MethodGenerator, Label> _action;

	private LogicOperator(BiConsumer<MethodGenerator, Label> action) {
		_action = action;
	}

	void run(MethodGenerator g, Label label) {
		_action.accept(g, label);
	}

//	  AreNotEqual, GreaterThan, LessThan, GreaterThanOrEqualTo, LessThanOrEqualTo

	public static LogicOperator IsNull = new LogicOperator((g, label) -> {
		g.evalStack().validateRefs(1);
		g.visitor().visitJumpInsn(Opcodes.IFNULL, label);
	});

	public static LogicOperator AreEqual = new LogicOperator((g, label) -> {
		var cls = g.evalStack().matchType(2);
		var visitor = g.visitor();
		if (!cls.isPrimitive()) {
			visitor.visitJumpInsn(Opcodes.IF_ACMPEQ, label);
		} else if (cls == long.class) {
			visitor.visitJumpInsn(Opcodes.LCMP, label);
		} else if (cls == int.class || cls == byte.class || cls == short.class || cls == char.class) {
			visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, label);
		} else if (cls == float.class) {
			visitor.visitInsn(Opcodes.FCMPL);// 将前面两个浮点数比较，如果相等，那么就是0
			visitor.visitVarInsn(Opcodes.FLOAD, 0);
			g.visitor().visitJumpInsn(Opcodes.IF_ACMPEQ, label); // 如果结果为0,那么就是相等
		}

		g.evalStack().pop(2); // 执行完了，弹出2个操作数
	});

}
