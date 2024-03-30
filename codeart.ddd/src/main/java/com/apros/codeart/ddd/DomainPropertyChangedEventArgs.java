package com.apros.codeart.ddd;

public class DomainPropertyChangedEventArgs {

	private Object _newValue;

	public Object newValue() {
		return _newValue;
	}

	private Object _oldValue;

	public Object oldValue() {
		return _oldValue;
	}

	private DomainProperty _property;

	/**
	 * 
	 * 获取发生值更改的领域项属性的标识符
	 * 
	 * @return
	 */
	public DomainProperty property() {
		return _property;
	}

	public DomainPropertyChangedEventArgs(DomainProperty property, Object newValue, Object oldValue) {
		this._property = property;
		this._newValue = newValue;
		this._oldValue = oldValue;
	}
}
