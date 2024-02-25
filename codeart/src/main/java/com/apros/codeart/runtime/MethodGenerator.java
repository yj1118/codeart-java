package com.apros.codeart.runtime;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.collect.Iterables;

public class MethodGenerator implements AutoCloseable {

	private boolean _isStatic;

	private MethodVisitor _visitor;

	private Class<?> _returnClass;

	private HashMap<String, VarInfo> _vars;

	/**
	 * 执行栈
	 */
	private LinkedList<Class<?>> _stack;

	private int _max_stack_count = 0;

	private void stack_push(Class<?> cls) {
		_stack.push(cls);
		if (_stack.size() > _max_stack_count)
			_max_stack_count = _stack.size();
	}

	private Class<?> stack_pop() {
		return _stack.pop();
	}

	MethodGenerator(MethodVisitor visitor, int access, Class<?> returnClass, Iterable<Argument> args) {
		_visitor = visitor;
		_isStatic = (access & Opcodes.ACC_STATIC) != 0;
		_returnClass = returnClass;
		init(args);
	}

	private void init(Iterable<Argument> args) {
		_vars = new HashMap<>();
		// varIndex是方法执行中，变量在变量表的位置
		int offset = _isStatic ? 0 : 1; // 实例方法的第一个变量是this
		for (var i = 0; i < Iterables.size(args); i++) {
			var varIndex = i + offset;
			var arg = Iterables.get(args, i);
			var info = new VarInfo(varIndex, arg.getType());
			_vars.put(arg.getName(), info);
		}
		_stack = new LinkedList<>();
		_visitor.visitCode();
	}

	private VarInfo getVar(String name) {
		var info = _vars.get(name);
		if (info == null)
			throw new IllegalArgumentException(strings("VariableNotFound", name));
		return info;
	}

	public void load_this() {
		if (_isStatic)
			throw new IllegalArgumentException(strings("CannotInvokeStatic"));
		_visitor.visitVarInsn(Opcodes.ALOAD, 0);
	}

	/**
	 * 加载参数
	 * 
	 * @param index
	 */
	public void load_parameter(String name) {
		load_var(name); // 加载参数就是加载变量
	}

	/**
	 * 加载引用类型的变量
	 * 
	 * @param index
	 */
	public void load_var(String name) {
		var info = getVar(name);
		if (info.isRef())
			_visitor.visitVarInsn(Opcodes.ALOAD, info.getIndex());
		else {
			_visitor.visitVarInsn(Opcodes.ILOAD, info.getIndex());
		}
		stack_push(info.getType());
	}

	/**
	 * 声明变量
	 * 
	 * @param name
	 * @param type
	 */
	public void var(String name, Class<?> type) {
		var varCount = _vars.size();
		var offset = _isStatic ? 0 : 1;
		var valueIndex = offset + varCount;
		_vars.put(name, new VarInfo(valueIndex, type));

		var descriptor = DynamicUtil.getDescriptor(type);
		_visitor.visitLocalVariable(name, descriptor, null, null, null, valueIndex);
	}

	public MethodGenerator load_field_value(String express) {
		String[] temp = express.split("\\.");
		return load_field_value(temp[0], temp[1]);
	}

	/**
	 * 加载变量上的字段的值
	 * 
	 * @param varName
	 * @param fieldName
	 */
	private MethodGenerator load_field_value(String varName, String fieldName) {

		try {
			var info = getVar(varName);
			Class<?> objectType = info.getType();
			var field = objectType.getDeclaredField(fieldName);

			Class<?> fieldType = field.getType();
			String typeDescriptor = Type.getDescriptor(fieldType); // 类似："Ljava/lang/String;"
			_visitor.visitFieldInsn(Opcodes.GETFIELD, objectType.getName(), field.getName(), typeDescriptor);

			stack_push(fieldType);
		} catch (Exception ex) {
			throw propagate(ex);
		}
		return this;
	}

	/**
	 * 执行实例方法
	 * 
	 * @param express
	 * @return
	 */
	public MethodGenerator invoke(String express, Runnable loadParameters) {
		String[] temp = express.split("\\.");
		return invoke(temp[0], temp[1], loadParameters);
	}

	/**
	 * 执行实例方法
	 * 
	 * @param varName
	 * @param methodName
	 * @return
	 */
	private MethodGenerator invoke(String varName, String methodName, Runnable loadParameters) {

		try {

			this.load_var(varName); // 先加载变量自身，作为实例方法的第一个参数（this）
			var info = getVar(varName);

			// 加载参数
			loadParameters.run();

			var cls = info.getType();

			var isInterface = cls.isInterface();
			var opcode = isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;

			var argCount = _stack.size() - 1;
			var argClasses = new Class<?>[argCount];

			var pointer = argCount - 1;
			// 弹出栈，并且收集参数
			while (_stack.size() > 0) {
				var type = stack_pop();
				if (_stack.size() == 0)
					break; // 不收集最后一个，因为这是对象自身，不是传递的参数，不能作为方法的参数查找
				argClasses[pointer] = type;
				pointer--;
			}

			Method method = cls.getMethod(methodName, argClasses);

			var descriptor = DynamicUtil.getMethodDescriptor(method);

			_visitor.visitMethodInsn(opcode, info.getType().getName(), methodName, descriptor, isInterface);

		} catch (Exception ex) {
			throw propagate(ex);
		}
		return this;
	}

	public void close() {
		_visitor.visitInsn(Opcodes.RETURN);
		_visitor.visitMaxs(_max_stack_count, _vars.size());
		_visitor.visitEnd();

		_stack.clear();
		_vars.clear();
		_max_stack_count = 0;
		_visitor.visitEnd();
	}

	private static class VarInfo {
		private Class<?> _type;

		public boolean isRef() {
			return !_type.isPrimitive();
		}

		private int _index;

		public int getIndex() {
			return _index;
		}

		public Class<?> getType() {
			return _type;
		}

		public VarInfo(int index, Class<?> type) {
			_index = index;
			_type = type;
		}

	}

}
