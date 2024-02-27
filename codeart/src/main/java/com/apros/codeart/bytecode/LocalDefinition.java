package com.apros.codeart.bytecode;

class LocalDefinition implements IVariable {
	private final MethodGenerator _owner;
	private final IVariable _innerVariable;

	public LocalDefinition(MethodGenerator owner, IVariable innerVariable) {
		_owner = owner;
		_innerVariable = innerVariable;
		_inScope = true;
	}

	private boolean _inScope;

	public boolean getInScope() {
		return _inScope;
	}

	public void setInScope(boolean inScope) {
		_inScope = inScope;
	}

	public Class<?> getType() {
		return _innerVariable.getType();
	}

	public String getName() {
		return _innerVariable.getName();
	}

	public void initialize() {
		_innerVariable.initialize();
	}

	@Override
	public void load() {
		_innerVariable.load();
		_owner.stack_frame_push(this.getType()); // 维护MethodGenerator自身的计算堆栈
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
		_innerVariable.beginAssign();
	}

	public void endAssign() {
//		StackAssert.IsAssignable(_owner._evalStack.Pop(), new StackItem(Type, LoadOptions.Default), true);
		_innerVariable.endAssign();
	}
}
