package com.apros.codeart.bytecode;

import java.util.function.Function;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public final class LogicOperator {

	private Function<MethodGenerator, Label> _action;

	private LogicOperator(Function<MethodGenerator, Label> action) {
		_action = action;
	}

	Label run(MethodGenerator g) {
		return _action.apply(g);
	}

//	  AreNotEqual, GreaterThan, LessThan, GreaterThanOrEqualTo, LessThanOrEqualTo

	public static LogicOperator IsNull = new LogicOperator((g) -> {
		g.evalStack().validateRefs(1);
		var label = new Label();
		g.visitor().visitJumpInsn(Opcodes.IFNULL, label);
		return label;
	});

	public static LogicOperator AreEqual = new LogicOperator((g) -> {
		var cls = g.evalStack().matchType(2);
		var ifLabel = new Label();
		var visitor = g.visitor();
		if (!cls.isPrimitive()) {
			visitor.visitJumpInsn(Opcodes.IF_ACMPEQ, ifLabel);
		} else if (cls == long.class) {
			visitor.visitJumpInsn(Opcodes.LCMP, ifLabel);
		} else if (cls == int.class || cls == byte.class || cls == short.class || cls == char.class) {
			visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, ifLabel);
		} else if (cls == float.class) {
			visitor.visitInsn(Opcodes.FCMPL);// 将前面两个浮点数比较，如果相等，那么就是0
			visitor.visitVarInsn(Opcodes.FLOAD, 0);
			g.visitor().visitJumpInsn(Opcodes.IF_ACMPEQ, ifLabel); // 如果结果为0,那么就是相等
		}

		g.evalStack().pop(2); // 执行完了，弹出2个操作数

		return ifLabel;
	});

}
