package apros.codeart.bytecode;

import static apros.codeart.i18n.Language.strings;
import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.collect.Iterables;

import apros.codeart.i18n.Language;
import apros.codeart.runtime.DynamicUtil;
import apros.codeart.runtime.FieldUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.TriConsumer;

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

	private ClassGenerator _owner;

	MethodGenerator(ClassGenerator owner, MethodVisitor visitor, int access, Class<?> returnClass,
			Iterable<MethodParameter> prms) {
		_owner = owner;
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
		_visitor.visitCode(); // 表示开始方法的代码生成
	}

	public void loadThis() {
		if (_isStatic)
			throw new IllegalArgumentException(strings("codeart", "CannotInvokeStatic"));
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
		MethodParameter prm = Iterables.get(_prms, prmIndex);
		loadVariable(prm.getName()); // 加载参数就是加载变量
	}

	/**
	 * 加载变量
	 * 
	 * @param index
	 */
	public Variable loadVariable(String name) {
		Variable local = _scopeStack.getVar(name);
		local.load();
		return local;
	}

	public void load(boolean value) {
		_visitor.visitInsn(value ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
		_evalStack.push(boolean.class);
	}

	public void load(byte value) {
		_visitor.visitLdcInsn(value);
		_evalStack.push(byte.class);
	}

	public void load(int value) {
		_visitor.visitLdcInsn(value);
		_evalStack.push(int.class);
	}

	public void load(float value) {
		_visitor.visitLdcInsn(value);
		_evalStack.push(float.class);
	}

	public void load(double value) {
		_visitor.visitLdcInsn(value);
		_evalStack.push(double.class);
	}

	public void load(long value) {
		// 加载 long 类型的常量，先加载低位部分
		_visitor.visitLdcInsn(value & 0xFFFFFFFFL);
		// 将 long 类型的常量转换为 long 类型并加载
		_visitor.visitLdcInsn(value >>> 32);

		// 将操作数栈中的两个 long 类型的常量拼接成一个 long 类型的值
		_visitor.visitInsn(Opcodes.LOR);
		_evalStack.push(long.class);
	}

	public void load(IVariable value) {
		value.load();
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

	public void load(Class<?> value) {
		_visitor.visitLdcInsn(value);
		_evalStack.push(Class.class);
	}

	/**
	 * 声明变量
	 * 
	 * @param name
	 * @param type
	 */
	public Variable declare(Class<?> type, String name) {

		return _scopeStack.declare(type, name);
	}

	public Variable declare(Class<?> type) {

		var name = String.format("var%d", _locals.size());
		return declare(type, name);
	}

	public MethodGenerator loadField(String express) {
		String[] temp = express.split("\\.");
		return loadField(temp[0], temp[1]);
	}

	/**
	 * 为变量赋值
	 * 
	 * @param name
	 */
	public void assign(String varName, Runnable loadValue) {
		var local = _scopeStack.getVar(varName);
		loadValue.run();
		local.save();
	}

	public void assign(IVariable local, Runnable loadValue) {
		loadValue.run();
		local.save();
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

			var accessor = FieldUtil.getFieldGetter(objectType, fieldName);

			if (accessor.isField()) {
				var field = objectType.getDeclaredField(fieldName);

				Class<?> fieldType = field.getType();
				String typeDescriptor = Type.getDescriptor(fieldType); // 类似："Ljava/lang/String;"
				String owner = Type.getInternalName(objectType);
				_visitor.visitFieldInsn(Opcodes.GETFIELD, owner, field.getName(), typeDescriptor);
				_evalStack.pop(); // 执行完毕后，变量就被弹出了
				_evalStack.push(fieldType);// 值进来了
			} else {
				Method method = accessor.getMethod();
				var opcode = Opcodes.INVOKEVIRTUAL;

				var descriptor = DynamicUtil.getMethodDescriptor(method);
				var owner = DynamicUtil.getInternalName(objectType); // info.getType().getName()

				_visitor.visitMethodInsn(opcode, owner, method.getName(), descriptor, false);

				_evalStack.pop(); // 执行完毕后，变量就被弹出了
				_evalStack.push(method.getReturnType());// 值进来了

			}

		} catch (Exception ex) {
			throw propagate(ex);
		}
		return this;
	}

	public MethodGenerator assignField(String express, Runnable loadValue) {
		String[] temp = express.split("\\.");
		return assignField(temp[0], temp[1], loadValue);
	}

	public MethodGenerator assignField(IVariable local, String fieldName, Runnable loadValue) {
		return assignField(() -> {
			local.load();
		}, fieldName, loadValue);
	}

	public MethodGenerator assignField(String varName, String fieldName, Runnable loadValue) {
		return assignField(() -> {
			this.loadVariable(varName);
		}, fieldName, loadValue);
	}

	public MethodGenerator assignField(Runnable loadOwner, String fieldName, Runnable loadValue) {

		try {
			// 先加载变量
			loadOwner.run();

			var objectType = _evalStack.peek().getValueType();

			loadValue.run();

			var accessor = FieldUtil.getFieldSetter(objectType, fieldName);
			if (accessor.isField()) {
				var field = accessor.getField();
				Class<?> fieldType = field.getType();

				String typeDescriptor = Type.getDescriptor(fieldType); // 类似："Ljava/lang/String;"
				String owner = Type.getInternalName(objectType);
				_visitor.visitFieldInsn(Opcodes.PUTFIELD, owner, field.getName(), typeDescriptor);
			} else {
				Method method = accessor.getMethod();
				var opcode = Opcodes.INVOKEVIRTUAL;

				var descriptor = DynamicUtil.getMethodDescriptor(method);
				var owner = DynamicUtil.getInternalName(objectType); // info.getType().getName()

				_visitor.visitMethodInsn(opcode, owner, method.getName(), descriptor, false);
			}

			_evalStack.pop(2); // 执行完毕后，目标和变量就被弹出了

		} catch (Exception ex) {
			throw propagate(ex);
		}
		return this;
	}

	public MethodGenerator assignStaticField(String fieldName, String fieldTypeName, Runnable loadValue) {

		loadValue.run();

		_visitor.visitFieldInsn(Opcodes.PUTSTATIC, _owner.getClassName(), fieldName,
				String.format("L%s;", fieldTypeName));
		return this;
	}

	public MethodGenerator assignStaticField(String fieldName, Class<?> fieldType, Runnable loadValue) {

		loadValue.run();

		_visitor.visitFieldInsn(Opcodes.PUTSTATIC, _owner.getClassName(), fieldName,
				DynamicUtil.getInternalName(fieldType));

		_evalStack.pop();

		return this;
	}

	/**
	 * 
	 * 加载方法执行时的参数类型信息
	 * 
	 * @return
	 */
	private Class<?>[] getArgClasses(int offset) {
		var argCount = _evalStack.size() - offset;
		var argClasses = new Class<?>[argCount];

		var pointer = argCount;
		// 弹出栈，并且收集参数
		while (pointer > 0) {
			var item = _evalStack.pop();
			pointer--;
			argClasses[pointer] = item.getValueType();
		}
		return argClasses;
	}

	/**
	 * 
	 * 执行 super() 方法
	 * 
	 * @param loadParameters
	 * @return
	 */
	public MethodGenerator invokeSuper() {
		return invokeSuper(null);
	}

	/**
	 * 
	 * 执行 super() 方法
	 * 
	 * @param loadParameters
	 * @return
	 */
	public MethodGenerator invokeSuper(Runnable loadParameters) {
		try {

			_evalStack.enterFrame(); // 新建立栈帧
			this.loadThis(); // 加载自身

			// 加载参数
			if (loadParameters != null)
				loadParameters.run();

			var argClasses = getArgClasses(1); // 栈顶第一个值是this，不作为参数，所以1

			var descriptor = DynamicUtil.getConstructorDescriptor(argClasses);

			var superTypeName = DynamicUtil.getInternalName(_owner.superClass());

			_visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superTypeName, "<init>", descriptor, false);

			_evalStack.exitFrame(); // 调用完毕，离开栈帧

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

	public MethodGenerator invoke(IVariable local, String methodName, Runnable loadParameters) {
		return invoke(() -> {
			local.load();
		}, methodName, loadParameters);
	}

	public MethodGenerator invoke(String varName, String methodName, Runnable loadParameters) {
		return invoke(() -> {
			this.loadVariable(varName);
		}, methodName, loadParameters);
	}

	public MethodGenerator invoke(Runnable loadTarget, String methodName, Runnable loadParameters) {

		try {

			_evalStack.enterFrame(); // 新建立栈帧
			loadTarget.run(); // 先加载目标，作为实例方法的第一个参数（this）
			var cls = _evalStack.peek().getValueType();

			// 加载参数
			if (loadParameters != null)
				loadParameters.run();

			var argClasses = getArgClasses(1); // 栈顶第一个值是this，不作为参数，所以1

			Method method = cls.getMethod(methodName, argClasses);
			var isInterface = cls.isInterface();
			var opcode = isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;

			var descriptor = DynamicUtil.getMethodDescriptor(method);
			var owner = DynamicUtil.getInternalName(cls); // info.getType().getName()

			_visitor.visitMethodInsn(opcode, owner, methodName, descriptor, isInterface);

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

	public MethodGenerator invokeStatic(Class<?> targetType, String methodName, Runnable loadParameters) {

		try {

			_evalStack.enterFrame(); // 新建立栈帧

			// 加载参数
			if (loadParameters != null)
				loadParameters.run();

			var argClasses = getArgClasses(0);

			Method method = targetType.getMethod(methodName, argClasses);

			var descriptor = DynamicUtil.getMethodDescriptor(method);
			var owner = DynamicUtil.getInternalName(targetType); // info.getType().getName()

			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, owner, methodName, descriptor, false);

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

	public void each(Runnable loadTarget, Class<?> elementType, Consumer<Variable> action) {

		var scopeDepth = _scopeStack.getDepth();

		// 1.加载需要遍历的目标
		loadTarget.run();

		// 2.执行遍历方法iterator()
		_visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);

		_evalStack.pop(); // 弹出目标
		_evalStack.push(Iterator.class);

		var iterator = this.declare(Iterator.class);
		iterator.save(); // 存储迭代器

		var loopStartLabel = new Label();
		_scopeStack.enter(loopStartLabel); // 进入循环代码段

		iterator.load(); // 加载迭代器

		var endLabel = new Label();

		_visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
		_evalStack.pop(); // 迭代器弹出

		_evalStack.push(boolean.class); // 存入iterator.hasNext()的结果
		_visitor.visitJumpInsn(Opcodes.IFEQ, endLabel); // 如果为0，那么直接跳到结束
		_evalStack.pop(); // 弹出iterator.hasNext()的结果

		iterator.load(); // 加载迭代器
		_visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);

		_evalStack.pop(); // 迭代器被弹出

		_evalStack.push(Object.class); // 压入当前遍历到的对象
		// 存入变量
		Variable local = null;
		if (elementType == Object.class) {
			local = this.declare(Object.class); // 不用起名字
			local.save();
		} else {
			_visitor.visitTypeInsn(Opcodes.CHECKCAST, DynamicUtil.getInternalName(elementType));
			local = this.declare(elementType); // 不用起名字
			local.save();
		}

		action.accept(local); // 执行用户方法

		_scopeStack.exit(); // 本次循环体结束

		_scopeStack.enter(); // 建立新的循环体

		_visitor.visitJumpInsn(Opcodes.GOTO, loopStartLabel);

		_scopeStack.exit();

		_visitor.visitLabel(endLabel);

		StackAssert.assertClean(this);

		ScopeAssert.assertDepth(_scopeStack, scopeDepth);
	}

	public void each(String varName, Class<?> elementType, Consumer<Variable> action) {
		each(() -> {
			this.loadVariable(varName);
		}, elementType, action);
	}

	public void loop(IVariable local, TriConsumer<Variable, Variable, Variable> action) {
		loop(() -> {
			local.load();
		}, action);
	}

	/**
	 * 
	 */
	public void loop(Runnable loadList, TriConsumer<Variable, Variable, Variable> action) {
		_scopeStack.enter();

		var endLabel = _scopeStack.getEndLabel();

		var i = this.declare(int.class);
		this.load(0);
		i.save();

		// for 循环开始
		var loopStartLabel = new Label();
		_scopeStack.enter(loopStartLabel); // 标记循环开始处

		var length = this.declare(int.class);
		// 以下代码是加载长度 start
		loadList.run(); // 加载集合
		var listType = _evalStack.peek().getValueType();
		if (listType.isArray()) {
			_visitor.visitInsn(Opcodes.ARRAYLENGTH); // 调用数组长度方法
			_evalStack.push(int.class);
		} else {
			_visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "size", "()I", true); // 调用 List.size()
			_evalStack.push(int.class);
		}
		length.save();
		_evalStack.pop();
		// 以上代码是加载长度 end

		i.load(); // 加载局部变量
		length.load();
		_visitor.visitJumpInsn(Opcodes.IF_ICMPGE, endLabel); // 如果 i>=length，则跳出循环

		_evalStack.pop(2);

		loadList.run(); // 加载集合
		Class<?> elementType = TypeUtil.resolveElementType(listType);
		i.load();
		if (listType.isArray()) {
			// 获取数组元素[i]
			if (!elementType.isPrimitive()) {
				// 注意，是AALOAD简而言之，
				// Opcodes.ALOAD 用于加载局部变量中的引用类型数据，
				// 而 Opcodes.AALOAD
				// 用于加载数组中的引用类型元素。
				_visitor.visitInsn(Opcodes.AALOAD);
			} else {
				if (elementType == int.class)
					_visitor.visitInsn(Opcodes.ILOAD);
				else if (elementType == long.class)
					_visitor.visitInsn(Opcodes.LLOAD);
				else if (elementType == float.class)
					_visitor.visitInsn(Opcodes.FLOAD);
				else if (elementType == double.class)
					_visitor.visitInsn(Opcodes.DLOAD);

				throw new IllegalArgumentException(strings("codeart", "UnknownException"));
			}

		} else {
			// 调用 List.get(int index) 方法
			_visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
		}
		_evalStack.pop(2);
		_evalStack.push(elementType);

		var item = this.declare(elementType);
		item.save();

		action.accept(item, i, length);

		// 对变量 i 进行自增操作
		i.load();
		_visitor.visitInsn(Opcodes.ICONST_1); // 将常量 1 推送到栈顶
		_evalStack.push(int.class);
		_visitor.visitInsn(Opcodes.IADD); // 将栈顶的两个整数值相加，并将结果压入栈顶
		i.save();
		_evalStack.pop();

		_visitor.visitJumpInsn(Opcodes.GOTO, loopStartLabel); // 跳到循环开始处

		_scopeStack.exit();

		_scopeStack.exit();
	}

	public void loop(IVariable length, Consumer<Variable> action) {
		loop(() -> {
			length.load();
		}, action);
	}

	/**
	 * for(var i=0;i<length;i++){}
	 * 
	 * @param loadLength
	 * @param action
	 */
	public void loop(Runnable loadLength, Consumer<Variable> action) {
		_scopeStack.enter();

		var endLabel = _scopeStack.getEndLabel();

		var i = this.declare(int.class);
		this.load(0);
		i.save();

		// for 循环开始
		var loopStartLabel = new Label();
		_scopeStack.enter(loopStartLabel); // 标记循环开始处

		i.load(); // 加载局部变量
		loadLength.run();
		_visitor.visitJumpInsn(Opcodes.IF_ICMPGE, endLabel); // 如果 i>=length，则跳出循环

		_evalStack.pop(2);

		action.accept(i);

		// 对变量 i 进行自增操作
		i.load();
		_visitor.visitInsn(Opcodes.ICONST_1); // 将常量 1 推送到栈顶
		_evalStack.push(int.class);
		_visitor.visitInsn(Opcodes.IADD); // 将栈顶的两个整数值相加，并将结果压入栈顶
		i.save();
		_evalStack.pop();

		_visitor.visitJumpInsn(Opcodes.GOTO, loopStartLabel); // 跳到循环开始处

		_scopeStack.exit();

		_scopeStack.exit();
	}

	public void increment(IVariable local) {
		local.load();
		_visitor.visitInsn(Opcodes.ICONST_1); // 将常量 1 推送到栈顶
		_evalStack.push(int.class);
		_visitor.visitInsn(Opcodes.IADD); // 将栈顶的两个整数值相加，并将结果压入栈顶
		_evalStack.pop(2);
		_evalStack.push(int.class);
		local.save();
	}

	public void print(String message) {
		print(() -> {
			this.load(message);
		});
	}

	public void print(IVariable v) {
		print(() -> {
			v.load();
		});
	}

	/**
	 * 将栈顶的值转换为toString
	 */
	public void castString() {
		castString(null);
	}

	public void castString(Runnable load) {
		if (load != null)
			load.run();
		var targetType = _evalStack.peek().getValueType();

		if (targetType == String.class)
			return;

		if (!targetType.isPrimitive()) {
			_visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;",
					false);
			_evalStack.pop();
			_evalStack.push(String.class);
			return;
		}

		if (targetType == int.class) {
			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;",
					false);
		} else if (targetType == long.class) {
			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;",
					false);
		} else if (targetType == boolean.class) {
			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;",
					false);
		} else if (targetType == byte.class) {
			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "toString", "(B)Ljava/lang/String;",
					false);
		} else if (targetType == float.class) {
			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;",
					false);
		} else if (targetType == double.class) {
			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;",
					false);
		} else if (targetType == short.class) {
			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "toString", "(S)Ljava/lang/String;",
					false);
		} else if (targetType == char.class) {
			_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Char", "toString", "(C)Ljava/lang/String;",
					false);
		} else
			throw new IllegalStateException(Language.strings("codeart", "UnknownException"));

		_evalStack.pop();
		_evalStack.push(String.class);

	}

	public void print(Runnable loadMessage) {
		// 获取 System.out 对象
		_visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

		_evalStack.push(System.out.getClass());

		loadMessage.run();
		castString();

		// 调用 PrintStream.println() 方法
		_visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V",
				false);

		_evalStack.pop(2);
	}

//	 #endregion

	public MethodGenerator newObject(String objectTypeName) {
		return newObject(objectTypeName, null);
	}

	/**
	 * 
	 * 该方法主要是为了解决正在建立的类A有字段要引用类A，导致还没有具体的Class<?>的问题
	 * 
	 * @param objectTypeName
	 * @param loadCtorPrms
	 * @return
	 */
	public MethodGenerator newObject(String objectTypeName, Runnable loadCtorPrms) {
		StackAssert.assertCount(_evalStack, 0);

		// 加载要创建对象的类的类型到操作数栈上
		_visitor.visitTypeInsn(Opcodes.NEW, objectTypeName);
		_evalStack.push(Object.class); // 为了保证栈同步，这里放入Object.class占位

		// 复制操作数栈顶的对象引用，因为构造函数调用需要消耗对象引用
		_visitor.visitInsn(Opcodes.DUP);
		_evalStack.push(Object.class); // 为了保证栈同步，这里放入Object.class占位

		// 加载构造函数参数到操作数栈上（如果有的话）
		if (loadCtorPrms != null)
			loadCtorPrms.run();

		var argClasses = getArgClasses(2);

		var descriptor = DynamicUtil.getConstructorDescriptor(argClasses);
		// 调用构造函数，并传入构造函数所需的参数
		_visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, objectTypeName, "<init>", descriptor, false);

		_evalStack.pop(1); // 参数已经弹出，这里弹出对象的引用（引用有两个，这里弹出其中一个）

		StackAssert.assertCount(_evalStack, 1); // 对象的引用在栈顶
		return this;
	}

	public MethodGenerator newObject(Class<?> objectType) {
		return newObject(objectType, null);
	}

	public MethodGenerator newObject(Class<?> objectType, Runnable loadCtorPrms) {
		if (objectType.isArray())
			throw new IllegalArgumentException(Language.strings("codeart", "UseNewArrayMethod"));

		StackAssert.assertCount(_evalStack, 0);

		var objectTypeName = DynamicUtil.getInternalName(objectType);
		// 加载要创建对象的类的类型到操作数栈上
		_visitor.visitTypeInsn(Opcodes.NEW, objectTypeName);
		_evalStack.push(objectType);

		// 复制操作数栈顶的对象引用，因为构造函数调用需要消耗对象引用
		_visitor.visitInsn(Opcodes.DUP);
		_evalStack.push(objectType);

		// 加载构造函数参数到操作数栈上（如果有的话）
		if (loadCtorPrms != null)
			loadCtorPrms.run();

		var argClasses = getArgClasses(2);

		var descriptor = DynamicUtil.getConstructorDescriptor(argClasses);
		// 调用构造函数，并传入构造函数所需的参数
		_visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, objectTypeName, "<init>", descriptor, false);

		_evalStack.pop(1); // 参数已经弹出，这里弹出对象的引用（引用有两个，这里弹出其中一个）

		StackAssert.assertCount(_evalStack, 1); // 对象的引用在栈顶
		return this;
	}

	public MethodGenerator newArray(Class<?> elementType, int arrayLength) {
		return newArray(elementType, () -> {
			this.load(arrayLength);
		});
	}

	public MethodGenerator newArray(Class<?> elementType, Runnable loadLength) {
		loadLength.run(); // 数组大小

		if (elementType.isPrimitive()) {
			var code = Opcodes.T_INT;
			Class<?> arrayClass = int[].class;
			if (elementType == int.class) {
				code = Opcodes.T_INT;
				arrayClass = int[].class;
			} else if (elementType == long.class) {
				code = Opcodes.T_LONG;
				arrayClass = long[].class;
			} else if (elementType == boolean.class) {
				code = Opcodes.T_BOOLEAN;
				arrayClass = boolean[].class;
			} else if (elementType == byte.class) {
				code = Opcodes.T_BYTE;
				arrayClass = byte[].class;
			} else if (elementType == short.class) {
				code = Opcodes.T_SHORT;
				arrayClass = short[].class;
			} else if (elementType == char.class) {
				code = Opcodes.T_CHAR;
				arrayClass = char[].class;
			} else if (elementType == float.class) {
				code = Opcodes.T_FLOAT;
				arrayClass = float[].class;
			} else if (elementType == double.class) {
				code = Opcodes.T_DOUBLE;
				arrayClass = double[].class;
			} else
				throw new IllegalStateException(Language.strings("codeart", "UnknownException"));

			_visitor.visitIntInsn(Opcodes.NEWARRAY, code); // 创建整数数组

			_evalStack.pop(); // 弹出长度参数
			_evalStack.push(arrayClass);
		} else {
			_visitor.visitTypeInsn(Opcodes.ANEWARRAY, DynamicUtil.getInternalName(elementType)); // 创建对象数组

			Class<?> arrayClass = Array.newInstance(elementType, 0).getClass();

			_evalStack.pop(); // 弹出长度参数
			_evalStack.push(arrayClass);
		}
		return this;
	}

	/**
	 * 创建一个ArrayList
	 * 
	 * @return
	 */
	public MethodGenerator newList() {
		// 实例化ArrayList
		_visitor.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
		_visitor.visitInsn(Opcodes.DUP);
		_visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
		_evalStack.push(ArrayList.class);
		return this;
	}

	public MethodGenerator asReadonlyList() {
		// 实例化ArrayList
		_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Collections", "unmodifiableList",
				"(Ljava/util/List;)Ljava/util/List;", false);
		_evalStack.pop();
		_evalStack.push(List.class);
		return this;
	}

	public MethodGenerator saveElement(IVariable array, IVariable i, IVariable element) {
		return saveElement(() -> {
			array.load();
		}, () -> {
			i.load();
		}, () -> {
			element.load();
		});
	}

	public MethodGenerator saveElement(Runnable loadArray, Runnable loadElementIndex, Runnable loadValue) {
		loadArray.run();
		loadElementIndex.run();
		loadValue.run();
		_visitor.visitInsn(Opcodes.IASTORE);
		return this;
	}

	private boolean _isbroken = false;

	public void broken() {
		_isbroken = true;
	}

	public void exit() {
		var size = _evalStack.size();
		if (size == 0) {
			if (_returnClass != void.class)
				throw new IllegalArgumentException(strings("codeart", "ReturnTypeMismatch"));
			_visitor.visitInsn(Opcodes.RETURN);
			return;
		}

		if (size > 1) {
			throw new IllegalArgumentException(strings("codeart", "ReturnError"));
		}
		var lastType = _evalStack.pop().getValueType(); // 返回就是弹出栈顶得值，给调用方用

		if (lastType != _returnClass) {
			throw new IllegalArgumentException(strings("codeart", "ReturnTypeMismatch"));
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
		throw new IllegalArgumentException(strings("codeart", "UnknownException"));
	}

	/**
	 * 
	 * 添加无参注解
	 * 
	 * @param annClass
	 */
	public void addAnnotation(Class<?> annClass) {
		addAnnotation(annClass, null);
	}

	public void addAnnotation(Class<?> annClass, Consumer<AnnotationOperation> fill) {
		String desc = Type.getDescriptor(annClass);
		var ag = _visitor.visitAnnotation(desc, true);
		if (fill != null)
			fill.accept(new AnnotationOperation(ag));
		ag.visitEnd();
	}

	/**
	 * 
	 * 调用 java.lang.Class.forName({@code className}) 方法，并将其结果压入操作数栈中。
	 * 
	 * @param className
	 */
	public void classForName(String className) {
		_visitor.visitLdcInsn(className);
		_visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
				"(Ljava/lang/String;)Ljava/lang/Class;", false);
	}

	public void close() {
		if (_isbroken)
			return; // 代码已毁坏
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
