package com.apros.codeart.bytecode;

import static com.apros.codeart.i18n.Language.strings;

class BorrowedLocal implements IVariable, AutoCloseable {
	private final LocalDefinition _owner;
	private boolean _outOfScope;

	public BorrowedLocal(LocalDefinition owner) {
		_owner = owner;
		_owner.setInScope(true);
	}

	private void validateScope() {
		if (_outOfScope)
			throw new IllegalStateException(strings("OutOfScope"));
	}

	@Override
	public void close() {
		// 释放局部变量
		// 局部变量并没有真正释放，指示把相关标记改变了
		_outOfScope = true;// 指示变量脱离了代码范围
		_owner.setInScope(false);// 指示变量不在代码范围中
	}

	@Override
	public Class<?> getType() {
		return _owner.getType();
	}

	@Override
	public String getName() {
		return _owner.getName();
	}

	@Override
	public void load() {
		validateScope();
		_owner.load();
	}

	@Override
	public void initialize() {
		_owner.initialize();
	}

	@Override
	public void beginAssign() {
		validateScope();
		_owner.beginAssign();
	}

	@Override
	public void endAssign() {
		validateScope();
		_owner.endAssign();
	}
}
