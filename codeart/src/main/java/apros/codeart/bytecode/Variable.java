package apros.codeart.bytecode;

import static apros.codeart.i18n.Language.strings;

import org.objectweb.asm.Opcodes;

import apros.codeart.runtime.DynamicUtil;

public class Variable implements IVariable {

	private final MethodGenerator _owner;
	private String _name;
	private final int _index;
	private final Class<?> _type;

	private boolean _inTable;

	void joinTable(ScopeStack.CodeScope scope) {
		if (!_inTable) {
			var descriptor = DynamicUtil.getDescriptor(_type);
			_owner.visitor().visitLocalVariable(_name, descriptor, null, scope.getStartLabel(), scope.getEndLabel(),
					_index);

			_inTable = true;

		}
	}

	public Variable(MethodGenerator owner, Class<?> type, String name, int index) {
		_owner = owner;
		_type = type;
		_name = name;
		_index = index;
		_inScope = true;
	}

	private boolean _inScope;

	public boolean getInScope() {
		return _inScope;
	}

	void setInScope(boolean inScope) {
		_inScope = inScope;
	}

	public boolean outOfScope() {
		return !_inScope;
	}

	protected void validateScope() {
		if (this.outOfScope())
			throw new IllegalStateException(strings("apros.codeart", "OutOfScope"));
	}

	public Class<?> getType() {
		return _type;
	}

	public String getInternalTypeName() {
		return DynamicUtil.getInternalName(_type);
	}

	public boolean isRef() {
		return !this._type.isPrimitive();
	}

	public String getName() {
		return _name;
	}

	void setName(String name) {
		_name = name;
	}

	public int getIndex() {
		return _index;
	}

	@Override
	public void load() {

		validateScope();

		var code = Util.getLoadCode(_type);

		_owner.visitor().visitVarInsn(code, _index);

		_owner.evalStack().push(_type);
	}

	@Override
	public void save() {

		validateScope();

		if (this.isRef())
			_owner.visitor().visitVarInsn(Opcodes.ASTORE, _index);
		else {
//			ISTORE: 将整数值存储到局部变量表中。
//			LSTORE: 将长整数值存储到局部变量表中。
//			FSTORE: 将单精度浮点数值存储到局部变量表中。
//			DSTORE: 将双精度浮点数值存储到局部变量表中。
//			ASTORE: 将引用类型值存储到局部变量表中。
			int codes = Util.getStoreCode(_type);

			_owner.visitor().visitVarInsn(codes, _index);
		}

		_owner.evalStack().pop(); // 存入变量后，栈顶的值就没了
	}

	@Override
	public void beginAssign() {
		validateScope();
//		_owner.beginAssign();
	}

	@Override
	public void endAssign() {
		validateScope();
//		_owner.endAssign();
	}

	@Override
	public void close() {
		// 释放局部变量
		// 局部变量并没有真正释放，指示把相关标记改变了
		_inScope = false;// 指示变量脱离了代码范围

	}

	/**
	 * 变量转换，该方法会把变量在栈顶转换成目标类型，但是不会更改变量在变量表中记录的变量类型
	 */
	@Override
	public IVariable cast(Class<?> targetType) {
		this.load();
		_owner.visitor().visitTypeInsn(Opcodes.CHECKCAST, DynamicUtil.getInternalName(targetType));

		var target = _owner.declare(targetType);
		target.save();

		return target;
	}
}
