package com.apros.codeart.bytecode;

import static com.apros.codeart.i18n.Language.strings;

import java.util.function.BiConsumer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
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
		StackAssert.assertRefs(g, 1);
		g.visitor().visitJumpInsn(Opcodes.IFNULL, label);
		g.evalStack().pop(); // 执行完了，弹出操作数
	});

	public static LogicOperator IsTrue = new LogicOperator((g, label) -> {
		StackAssert.assertCount(g, 1);
		g.visitor().visitJumpInsn(Opcodes.IFNE, label);
		g.evalStack().pop(); // 执行完了，弹出操作数
	});

	public static LogicOperator AreEqual = new LogicOperator((g, label) -> {
		Class<?> cls = g.evalStack().matchType(2);
		MethodVisitor visitor = g.visitor();
		if (!cls.isPrimitive()) {
			visitor.visitJumpInsn(Opcodes.IF_ACMPEQ, label);
		} else if (cls == long.class) {
			visitor.visitInsn(Opcodes.LCMP);
			visitor.visitJumpInsn(Opcodes.IFEQ, label);
		} else if (cls == int.class || cls == byte.class || cls == short.class || cls == char.class) {
			visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, label);
		} else if (cls == float.class) {
			visitor.visitInsn(Opcodes.FCMPL);// 将前面两个浮点数比较，如果相等，那么就是0
			g.visitor().visitJumpInsn(Opcodes.IFEQ, label); // 如果结果为0,那么就是相等
		} else if (cls == double.class) {
			visitor.visitInsn(Opcodes.DCMPL);
			g.visitor().visitJumpInsn(Opcodes.IFEQ, label); // 如果结果为0,那么就是相等
		}

		g.evalStack().pop(2); // 执行完了，弹出2个操作数
	});

	public static LogicOperator LessThan = new LogicOperator((g, label) -> {
		Class<?> cls = g.evalStack().matchType(2);
		MethodVisitor visitor = g.visitor();
		if (!cls.isPrimitive()) {
			throw new IllegalArgumentException(strings("codeart", "OperationMismatch", "LessThan", cls.getName()));
		} else if (cls == long.class) {
			visitor.visitInsn(Opcodes.LCMP); // 返回-1就是小于
			visitor.visitInsn(Opcodes.ICONST_M1);
			visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, label);
		} else if (cls == int.class || cls == byte.class || cls == short.class || cls == char.class) {
			visitor.visitJumpInsn(Opcodes.IF_ICMPLT, label);
		} else if (cls == float.class) {
			visitor.visitInsn(Opcodes.FCMPL);// 将前面两个浮点数比较，如果小于，那么就是-1
			visitor.visitInsn(Opcodes.ICONST_M1);
			visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, label);
		} else if (cls == double.class) {
			visitor.visitInsn(Opcodes.DCMPL);
			visitor.visitInsn(Opcodes.ICONST_M1);
			visitor.visitJumpInsn(Opcodes.IF_ICMPEQ, label);
		}

		g.evalStack().pop(2); // 执行完了，弹出2个操作数
	});

}
