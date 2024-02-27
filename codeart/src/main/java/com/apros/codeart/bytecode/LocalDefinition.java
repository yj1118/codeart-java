package com.apros.codeart.bytecode;

class LocalDefinition implements IVariable {
	private final MethodGenerator _owner;
	private final String _name;
	private final int _index;
	private final Class<?> _type;

	public LocalDefinition(MethodGenerator owner, Class<?> type, String name, int index) {
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

	public void setInScope(boolean inScope) {
		_inScope = inScope;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	public Class<?> getType() {
		return _type;
	}

	public String getName() {
		return _name;
	}

	public int getIndex() {
		return _index;
	}

	@Override
	public void load() {
//		_innerVariable.load();
		_owner.evalStack().push(this.getType()); // 维护MethodGenerator自身的计算堆栈
	}

//	public void store() {
////		StackAssert.IsAssignable(_owner._evalStack.Pop(), new StackItem(Type, LoadOptions.Default), true);
////		_innerVariable.Store();
//	}

//	public bool CanStore
//	{
//	    get { return _innerVariable.CanStore; }
//	}

	public void beginAssign() {
//		_innerVariable.beginAssign();
	}

	public void endAssign() {
//		StackAssert.IsAssignable(_owner._evalStack.Pop(), new StackItem(Type, LoadOptions.Default), true);
//		_innerVariable.endAssign();
	}

}
