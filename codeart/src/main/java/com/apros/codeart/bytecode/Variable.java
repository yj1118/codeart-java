package com.apros.codeart.bytecode;

import static com.apros.codeart.i18n.Language.strings;

import org.objectweb.asm.Opcodes;

public class Variable implements IVariable {

	private final MethodGenerator _owner;
	private String _name;
	private final int _index;
	private final Class<?> _type;

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
			throw new IllegalStateException(strings("OutOfScope"));
	}

	public Class<?> getType() {
		return _type;
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

		if (this.isRef())
			_owner.visitor().visitVarInsn(Opcodes.ALOAD, _index);
		else {
			_owner.visitor().visitVarInsn(Opcodes.ILOAD, _index);
		}

		_owner.evalStack().push(_type);
	}

	@Override
	public void save() {

		validateScope();

		if (this.isRef())
			_owner.visitor().visitVarInsn(Opcodes.ASTORE, _index);
		else {
			_owner.visitor().visitVarInsn(Opcodes.ISTORE, _index);
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
}
