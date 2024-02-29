package com.apros.codeart.bytecode;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.apros.codeart.runtime.DynamicUtil;
import com.google.common.collect.Iterables;

public class MethodGenerator implements AutoCloseable {

	private boolean _isStatic;

	public boolean isStatic() {
		return _isStatic;
	}

	private Iterable<MethodParameter> _prms;

	private MethodVisitor _visitor;

	MethodVisitor visitor() {
		return _visitor;
	}

	private Class<?> _returnClass;

	/**
	 * 局部变量的集合,该集合存放了所有声明过的局部变量
	 */
	private VariableCollection _locals = null;

	VariableCollection locals() {
		return _locals;
	}

	/**
	 * 调用栈
	 */
	private EvaluationStack _evalStack;

	public EvaluationStack evalStack() {
		return _evalStack;
	}

	private ScopeStack _scopeStack;

	public ScopeStack scopeStack() {
		return _scopeStack;
	}

	MethodGenerator(MethodVisitor visitor, int access, Class<?> returnClass, Iterable<MethodParameter> prms) {
		_visitor = visitor;
		_isStatic = (access & Opcodes.ACC_STATIC) != 0;
		_returnClass = returnClass;
		init(prms);
	}

	private void init(Iterable<MethodParameter> prms) {
		_prms = prms;
		_locals = new VariableCollection(this);
		_evalStack = new EvaluationStack();
		_scopeStack = new ScopeStack(this, prms);
		_visitor.visitCode();
	}

	public void loadThis() {
		if (_isStatic)
			throw new IllegalArgumentException(strings("CannotInvokeStatic"));
		_visitor.visitVarInsn(Opcodes.ALOAD, 0);
	}

	/**
	 * 加载参数
	 * 
	 * @param index
	 */
	public void loadParameter(String name) {
		loadVariable(name); // 加载参数就是加载变量
	}

	public void loadParameter(int prmIndex) {
		var prm = Iterables.get(_prms, prmIndex);
		loadVariable(prm.getName()); // 加载参数就是加载变量
	}

	/**
	 * 加载变量
	 * 
	 * @param index
	 */
	public Variable loadVariable(String name) {
		var local = _scopeStack.getVar(name);
		local.load();
		return local;
	}

	public void load(int value) {
		_visitor.visitLdcInsn(value);
		_evalStack.push(int.class);
	}

	/**
	 * 将 null 引用推送到操作数栈上
	 */
	public void loadNull() {
		_visitor.visitInsn(Opcodes.ACONST_NULL);
		_evalStack.push(Object.class);// 同步自身堆栈数据
	}

	public void load(String value) {
		if (value == null) {
			loadNull();
			return;
		}

		_visitor.visitLdcInsn(value);
		_evalStack.push(String.class);
	}

	/**
	 * 声明变量
	 * 
	 * @param name
	 * @param type
	 */
	public Variable declare(Class<?> type, String name) {

		var local = _scopeStack.declare(type, name);

		var descriptor = DynamicUtil.getDescriptor(local.getType());
		_visitor.visitLocalVariable(name, descriptor, null, null, null, local.getIndex());

		return local;
	}

	public Variable declare(Class<?> type) {

		var name = String.format("var_%d", _locals.size());
		return declare(type, name);
	}

	public MethodGenerator loadField(String express) {
		String[] temp = express.split("\\.");
		return loadField(temp[0], temp[1]);
	}

	/**
	 * 加载变量上的字段的值
	 * 
	 * @param varName
	 * @param fieldName
	 */
	public MethodGenerator loadField(String varName, String fieldName) {

		try {
			// 先加载变量
			this.loadVariable(varName);

			var local = _scopeStack.getVar(varName);
			Class<?> objectType = local.getType();
			var field = objectType.getDeclaredField(fieldName);

			Class<?> fieldType = field.getType();
			String typeDescriptor = Type.getDescriptor(fieldType); // 类似："Ljava/lang/String;"
			String owner = Type.getInternalName(objectType);
			_visitor.visitFieldInsn(Opcodes.GETFIELD, owner, field.getName(), typeDescriptor);

			_evalStack.pop(); // 执行完毕后，变量就被弹出了
			_evalStack.push(fieldType);// 值进来了

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

	public MethodGenerator invoke(int prmIndex, String methodName, Runnable loadParameters) {
		var prm = Iterables.get(_prms, prmIndex);
		return invoke(prm.getName(), methodName, loadParameters);
	}

	public MethodGenerator invoke(int prmIndex, Method method, Runnable loadParameters) {
		var prm = Iterables.get(_prms, prmIndex);
		return invoke(prm.getName(), method.getName(), loadParameters);
	}

	public MethodGenerator invoke(String varName, String methodName, Runnable loadParameters) {

		try {

			_evalStack.enterFrame(); // 新建立栈帧
			this.loadVariable(varName); // 先加载变量自身，作为实例方法的第一个参数（this）
			var info = _scopeStack.getVar(varName);

			// 加载参数
			if (loadParameters != null)
				loadParameters.run();

			var cls = info.getType();
			var argCount = _evalStack.size() - 1;
			var argClasses = new Class<?>[argCount];

			var pointer = argCount - 1;
			// 弹出栈，并且收集参数
			while (_evalStack.size() > 0) {
				var item = _evalStack.pop();
				if (_evalStack.size() == 0)
					break; // 不收集最后一个，因为这是对象自身，不是传递的参数，不能作为方法的参数查找
				argClasses[pointer] = item.getValueType();
				pointer--;
			}

			Method method = cls.getMethod(methodName, argClasses);
			var isInterface = cls.isInterface();
			var opcode = isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;

			var descriptor = DynamicUtil.getMethodDescriptor(method);
			var owner = DynamicUtil.getInternalName(info.getType()); // info.getType().getName()

			_visitor.visitMethodInsn(opcode, owner, method.getName(), descriptor, isInterface);

			_evalStack.exitFrame(); // 调用完毕，离开栈帧

			var returnType = method.getReturnType();
			if (returnType != void.class) {
				_evalStack.push(returnType); // 返回值会给与父级栈
			}

		} catch (Exception ex) {
			throw propagate(ex);
		}
		return this;
	}

	public void when(Supplier<LogicOperator> condition, Runnable trueAction, Runnable falseAction) {

		var op = condition.get();
		var trueStartLabel = new Label();
		op.run(this, trueStartLabel);

		// 先执行falseAction

		_scopeStack.enter();
		falseAction.run();
		_scopeStack.exit();

		var endLabel = new Label();

		_visitor.visitJumpInsn(Opcodes.GOTO, endLabel);

		_scopeStack.enter(trueStartLabel);
		trueAction.run();
		_scopeStack.exit();

		_visitor.visitLabel(endLabel);
	}

	public void when(Supplier<LogicOperator> condition, Runnable trueAction) {
		var op = condition.get();
		var trueStartLabel = new Label();
		op.run(this, trueStartLabel);
		var endLabel = new Label();
		_visitor.visitJumpInsn(Opcodes.GOTO, endLabel);
		_visitor.visitLabel(trueStartLabel);
		this._scopeStack.using(trueAction);

		_visitor.visitLabel(endLabel);
	}

//	 #region foreach

	public void each(Runnable loadTarget, Consumer<Variable> action) {

		var scopeDepth = _scopeStack.getDepth();

		// 1.加载需要遍历的目标
		loadTarget.run();

		// 2.执行遍历方法iterator()
		_visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "iterator", "()Ljava/util/Iterator;",
				true);

		_evalStack.pop(); // 弹出目标

		_scopeStack.enter(); // 进入循环代码段

		var elementType = Object.class;
		_evalStack.push(elementType);// 存入iterator() 方法返回的 Iterator 对象

		// 将栈顶的值存变量
		var local = this.declare(elementType); // 不用起名字
		local.save();

//		var loopStartLabel = new Label();
		var endLabel = new Label();

//		_visitor.visitLabel(loopStartLabel);

		action.accept(local);

		loadTarget.run(); // 继续加载需要遍历的变量，执行hasNext判断
		_visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
		_evalStack.pop(); // 遍历的对象变量被弹出
		_evalStack.push(boolean.class); // 栈顶有个布尔值，表示是否有下一条数据
		_visitor.visitJumpInsn(Opcodes.IFEQ, endLabel); // 如果为0，那么直接跳到结束
		_evalStack.pop(); // 弹出布尔值

		loadTarget.run(); // 为执行next加载目标
		_visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);

		_evalStack.pop(); // 目标被弹出
		_scopeStack.exit(); // 本次循环体结束

		_scopeStack.enter(); // 建立新的循环体

		_evalStack.push(elementType); // 存入next() 方法返回的 Iterator 对象
		local.save();

		_visitor.visitJumpInsn(Opcodes.GOTO, loopStartLabel);

		_scopeStack.exit();

		_visitor.visitLabel(endLabel);

		StackAssert.assertClean(_evalStack);

		ScopeAssert.assertDepth(_scopeStack, scopeDepth);
	}

	public void each(String varName, Consumer<Variable> action) {
		each(() -> {
			this.loadVariable(varName);
		}, action);
	}

	public void print(String message) {
		print(() -> {
			this.load(message);
		});
	}

	public void print(Runnable loadMessage) {
		// 获取 System.out 对象
		_visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

		_evalStack.push(System.out.getClass());

		loadMessage.run();

		// 调用 PrintStream.println() 方法
		_visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",
				false);

		_evalStack.pop(2);
	}

//	 #endregion

	public void exit() {
		var size = _evalStack.size();
		if (size == 0) {
			if (_returnClass != void.class)
				throw new IllegalArgumentException(strings("ReturnTypeMismatch"));
			_visitor.visitInsn(Opcodes.RETURN);
			return;
		}

		if (size > 1) {
			throw new IllegalArgumentException(strings("ReturnError"));
		}
		var lastType = _evalStack.pop().getValueType(); // 返回就是弹出栈顶得值，给调用方用

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

		_scopeStack = null;
		_evalStack = null;
		_visitor = null;
		_locals = null;
	}
}
