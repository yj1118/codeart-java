package com.apros.codeart.dto;

import java.util.Collections;
import java.util.function.Function;

import com.apros.codeart.context.ContextSession;
import com.apros.codeart.util.StringUtil;

final class DTEValue extends DTEntity {

	@Override
	public DTEntityType getType() {
		return DTEntityType.VALUE;
	}

	private String _valueCode;

	private boolean _valueIsString;

	private Object _value;

	public Object getValue() {
		if (_value == null) {
			_value = _valueIsString ? JSON.getValueByString(_valueCode) : JSON.getValueByNotString(_valueCode);
		}
		return _value;
	}

	public void setValue(Object value) {
		this.clearData();
		this._value = value;
	}

	private DTEValue(String name, String valueCode, boolean valueIsString, Object value) {
		this.setName(name);
		this._valueCode = valueCode;
		this._valueIsString = valueIsString;
		this._value = value;
	}

	@Override
	public DTEntity cloneImpl() {
//		// 原版本中是克隆了value，但是新版本中考虑到value要么是字符串，要么是其他值类型，要么是DTObject（仅在此情况下克隆），没有克隆的必要。
//		var value = this.getValue();
//		var dto = as(value, DTObject.class);
//		if (dto != null)
//			return obtain(this.getName(), dto.clone());
		return obtain(this.getName(), _valueCode, _valueIsString, _value);
	}

	@Override
	public void close() throws Exception {
		super.close();
		this.clearData();
	}

	@Override
	public void clearData() {
		_valueCode = null;
		_valueIsString = false;
		_value = null;
	}

	@Override
	public boolean hasData() {
		return _valueCode != null;
	}

	@Override
	public Iterable<DTEntity> finds(QueryExpression query) {
		if (query.onlySelf() || this.getName().equalsIgnoreCase(query.getSegment()))
			return this.getSelfAsEntities();// 查询自身
		return Collections.emptyList();
	}

	@Override
	public void fillCode(StringBuilder code, boolean sequential, boolean outputName) {
		String name = this.getName();
		if (outputName && !StringUtil.isNullOrEmpty(name))
			code.append(String.format("\"{0}\"", name));
		if (code.length() > 0)
			code.append(":");
		fillValueCode(code, sequential);
	}

	private void fillValueCode(StringBuilder code, boolean sequential) {
		if (_valueCode == null) {
			// 没有valueCode，说明是以obtainByValue的方式创造的，这时候直接将value转换成码
			JSON.writeValue(code, _value);
			return;
		}

		// 有valueCode
		if (_valueIsString) {
			code.append("\"");
			code.append(_valueCode); // _valueCode是已经解析好了的代码，直接输出
			code.append("\"");
			return;
		}

		// 不是字符串，直接输出
		code.append(_valueCode);
		return;

//		var value = this.getValue();
//		var dto = as(value, DTObject.class);
//		if (dto != null)
//			return dto.getCode(sequential, false);
//		return JSON.getCode(value);
	}

	@Override
	public void fillSchemaCode(StringBuilder code, boolean sequential, boolean outputName) {
		code.append(this.getName());
	}

	@Override
	public void setMember(QueryExpression query, Function<String, DTEntity> createEntity) {
		throw new UnsupportedOperationException("DTEValue.setMember");

	}

	@Override
	public void removeMember(DTEntity e) {
		throw new UnsupportedOperationException("DTEValue.removeMember");

	}

	public static DTEValue obtainByCode(String name, String valueCode, boolean valueIsString) {
		return obtain(name, valueCode, valueIsString, null);
	}

	public static DTEValue obtainByValue(String name, Object value) {
		return obtain(name, null, false, value);
	}

	private static DTEValue obtain(String name, String valueCode, boolean valueIsString, Object value) {
		return ContextSession.registerItem(new DTEValue(name, valueCode, valueIsString, value));
	}

}