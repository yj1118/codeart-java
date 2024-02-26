package com.apros.codeart.runtime;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Supplier;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.collect.Iterables;

public class MethodGenerator implements AutoCloseable {

	private boolean _isStatic;

	private MethodVisitor _visitor;

	MethodVisitor visitor() {
		return _visitor;
	}

	private Class<?> _returnClass;

	private HashMap<String, VarInfo> _vars;

	/**
	 * 执行栈
	 */
	private LinkedList<EvaluationStack> _stacks;

	private int _max_stack_deep = 0;

	private void stack_enter() {
		_stacks.push(new EvaluationStack());
		if (_stacks.size() > _max_stack_deep)
			_max_stack_deep = _stacks.size();
	}

	private void stack_exit() {
		_stacks.pop();
	}

	private EvaluationStack stack_current() {
		return _stacks.peek();
	}

	private int stack_current_size() {
		return stack_current().size();
	}

	private void stack_current_push(Class<?> cls) {
		stack_current().push(cls);
	}

	private Class<?> stack_current_pop() {
		return stack_current().pop();
	}

	/**
	 * 获取栈里得值得类型信息
	 * 
	 * @return
	 */
	Class<?>[] getValueTypes(int expectedCount) {
		return stack_current().getValueTypes(expectedCount);
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
		_stacks = new LinkedList<EvaluationStack>();
		_stacks.push(new EvaluationStack());
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

//	public void write(Consumer<MethodVisitor> write) {
//		write.accept(_visitor);
//	}

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
		stack_current_push(info.getType());
	}

	public void load_const(int value) {
		_visitor.visitLdcInsn(value);
		stack_current_push(int.class);
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
			// 先加载变量
			this.load_var(varName);

			var info = getVar(varName);
			Class<?> objectType = info.getType();
			var field = objectType.getDeclaredField(fieldName);

			Class<?> fieldType = field.getType();
			String typeDescriptor = Type.getDescriptor(fieldType); // 类似："Ljava/lang/String;"
			String owner = Type.getInternalName(objectType);
			_visitor.visitFieldInsn(Opcodes.GETFIELD, owner, field.getName(), typeDescriptor);

			stack_current_pop(); // 执行完后，变量就弹出了

			stack_current_push(fieldType); // 值进来了
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

	public MethodGenerator invoke(String express) {
		return invoke(express, null);
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

			stack_enter();
			this.load_var(varName); // 先加载变量自身，作为实例方法的第一个参数（this）
			var info = getVar(varName);

			// 加载参数
			if (loadParameters != null)
				loadParameters.run();

			var cls = info.getType();

			var isInterface = cls.isInterface();
			var opcode = isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;

			var argCount = stack_current_size() - 1;
			var argClasses = new Class<?>[argCount];

			var pointer = argCount - 1;
			// 弹出栈，并且收集参数
			while (stack_current_size() > 0) {
				var type = stack_current_pop();
				if (stack_current_size() == 0)
					break; // 不收集最后一个，因为这是对象自身，不是传递的参数，不能作为方法的参数查找
				argClasses[pointer] = type;
				pointer--;
			}

			Method method = cls.getMethod(methodName, argClasses);

			var descriptor = DynamicUtil.getMethodDescriptor(method);
			var owner = DynamicUtil.getInternalName(info.getType()); // info.getType().getName()

			_visitor.visitMethodInsn(opcode, owner, methodName, descriptor, isInterface);

			stack_exit(); // 调用完毕

			if (method.getReturnType() != void.class) {
				stack_current_push(method.getReturnType()); // 返回值会给与父级栈
			}

		} catch (Exception ex) {
			throw propagate(ex);
		}
		return this;
	}

	public void when(Supplier<LogicOperator> condition, Runnable ifAction, Runnable elseAction) {
		var op = condition.get();
		var ifLabel = op.run(this);
		var endLabel = new Label();

		elseAction.run();
		_visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
		_visitor.visitLabel(ifLabel);
		ifAction.run();
		_visitor.visitLabel(endLabel);
	}

	public void when(Supplier<LogicOperator> condition, Runnable ifAction) {
		var op = condition.get();
		var ifLabel = op.run(this);
		var endLabel = new Label();
		_visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
		_visitor.visitLabel(ifLabel);
		ifAction.run();
		_visitor.visitLabel(endLabel);
	}

	public void exit() {
		var size = stack_current_size();
		if (size == 0) {
			if (_returnClass != void.class)
				throw new IllegalArgumentException(strings("ReturnTypeMismatch"));
			_visitor.visitInsn(Opcodes.RETURN);
			return;
		}

		if (size > 1) {
			throw new IllegalArgumentException(strings("ReturnError"));
		}
		var lastType = stack_current_pop(); // 返回就是弹出栈顶得值，给调用方用

		if (lastType != _returnClass) {
			throw new IllegalArgumentException(strings("ReturnTypeMismatch"));
		}

		if (!lastType.isPrimitive()) {
			_visitor.visitInsn(Opcodes.ARETURN);
			return;
		}
		if (lastType == int.class) {
			_visitor.visitInsn(Opcodes.IRETURN);
			return;
		}
		if (lastType == long.class) {
			_visitor.visitInsn(Opcodes.LRETURN);
			return;
		}
		if (lastType == float.class) {
			_visitor.visitInsn(Opcodes.FRETURN);
			return;
		}
		if (lastType == double.class) {
			_visitor.visitInsn(Opcodes.DRETURN);
			return;
		}
		throw new IllegalArgumentException(strings("UnknownException"));
	}

	public void close() {
		exit();
		// 由于开启了COMPUTE_FRAMES ，所以只用调用visitMaxs即可，不必设置具体的值
		_visitor.visitMaxs(0, 0);
		_visitor.visitEnd();

		_stacks.clear();
		_vars.clear();
		_max_stack_deep = 0;
	}

	/**
	 * 检查栈顶至少值有多少个
	 * 
	 * @param count
	 */
	void validateLeast(int count) {
		if (stack_current_size() < count) {
			throw new IllegalStateException(strings("TypeMismatch"));
		}
	}

	void validateRefs(int expectedCount) {
		stack_current().validateRefs(expectedCount);
	}

	/**
	 * 查找栈顶上得元素，得到他们得类型，如果他们之间得类型不相同，报错
	 * 
	 * @param expectCount 期望栈顶有几个值
	 * @return
	 */
	Class<?> matchType(int expectedCount) {
		validateLeast(expectedCount);
		var actualTypes = this.getValueTypes(expectedCount);
		var targetType = actualTypes[0];
		for (var i = 1; i < actualTypes.length; i++) {
			if (targetType != actualTypes[i])
				throw new IllegalStateException(strings("TypeMismatch"));
		}
		return targetType;
	}

	void stack_pop(int count) {
		while (count > 0) {
			stack_current().pop();
			count--;
		}
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
