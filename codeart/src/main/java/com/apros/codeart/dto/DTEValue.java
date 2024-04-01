package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.function.Function;

import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ISO8601;
import com.apros.codeart.util.StringUtil;

/**
 * 
 */
final class DTEValue extends DTEntity {

	@Override
	public DTEntityType getType() {
		return DTEntityType.VALUE;
	}

	private String _valueCode;

	private boolean _valueCodeIsString;

	private Object _value;

	public Object getValueRef() {
		if (_value == null) {
			_value = _valueCodeIsString ? JSON.getValueByString(_valueCode) : JSON.getValueByNotString(_valueCode);
		}
		return _value;
	}

	/**
	 * 经过测试发现,xx.parse的运行的速度小于拆箱(xx)_value操作；
	 * 
	 * 时间消耗大概是1：3。
	 */

	public boolean getBoolean() {
		if (_value != null)
			return (boolean) _value;
		return Boolean.parseBoolean(_valueCode);
	}

	public byte getByte() {
		if (_value != null)
			return (byte) _value;
		return Byte.parseByte(_valueCode);
	}

	public short getShort() {
		if (_value != null)
			return (short) _value;
		return Short.parseShort(_valueCode);
	}

	public int getInt() {
		if (_value != null)
			return (int) _value;
		return Integer.parseInt(_valueCode);
	}

	public long getLong() {
		if (_value != null)
			return (long) _value;
		return Long.parseLong(_valueCode);
	}

	public float getFloat() {
		if (_value != null)
			return (float) _value;
		return Float.parseFloat(_valueCode);
	}

	public double getDouble() {
		if (_value != null)
			return (double) _value;
		return Double.parseDouble(_valueCode);
	}

	public Instant getInstant() {
		if (_value != null)
			return (Instant) _value;
		return ISO8601.getInstant(_valueCode);
	}

	public LocalDateTime getLocalDateTime() {
		if (_value != null)
			return (LocalDateTime) _value;
		return ISO8601.getLocalDateTime(_valueCode);
	}

	public String getString() {
		if (_value != null)
			return (String) _value;
		return _valueCode;
	}

	public char getChar() {
		if (_value != null)
			return (char) _value;
		return _valueCode.charAt(0);
	}

	/**
	 * 时间等引用类型通过该方法赋值
	 * 
	 * @param value
	 */
	public void setValueRef(Object value, boolean valueCodeIsString) {
		_valueCode = null;
		_valueCodeIsString = valueCodeIsString;
		_value = value;
	}

	/**
	 * 基类型和字符串都是通过该方法赋值的
	 * 
	 * @param valueCode
	 * @param valueIsString
	 */
	public void setValueCode(String valueCode, boolean valueCodeIsString) {
		_valueCode = valueCode;
		_valueCodeIsString = valueCodeIsString;
		_value = null;
	}

	private DTEValue(String name, String valueCode, boolean valueCodeIsString, Object value) {
		this.setName(name);
		this._valueCode = valueCode;
		this._valueCodeIsString = valueCodeIsString;
		this._value = value;
	}

	@Override
	public DTEntity cloneImpl() {
//		// 原版本中是克隆了value，但是新版本中考虑到value要么是字符串，要么是其他值类型，要么是DTObject（仅在此情况下克隆），没有克隆的必要。
//		var value = this.getValue();
//		var dto = as(value, DTObject.class);
//		if (dto != null)
//			return obtain(this.getName(), dto.clone());
		return obtain(this.getName(), _valueCode, _valueCodeIsString, _value);
	}

//	@Override
//	public void close() throws Exception {
//		super.close();
//		this.clearData();
//	}

	@Override
	public void clearData() {
		_valueCode = null;
		_valueCodeIsString = false;
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
			code.append(String.format("\"%s\"", name));
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
		if (_valueCodeIsString) {
			code.append("\"");
			code.append(_valueCode); // _valueCode是已经解析好了的代码，直接输出
			code.append("\"");
			return;
		}

		// 不是字符串，直接输出
		code.append(_valueCode);
		return;
	}

	public String getCode(boolean sequential, boolean outputName) {
		StringBuilder code = new StringBuilder();
		fillCode(code, sequential, outputName);
		return code.toString();
	}

	@Override
	public void fillSchemaCode(StringBuilder code, boolean sequential) {
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

	public static DTEValue obtainByCode(String name, String valueCode, boolean valueCodeIsString) {
		return obtain(name, valueCode, valueCodeIsString, null);
	}

	public static DTEValue obtainByValue(String name, Object value, boolean valueCodeIsString) {
		return obtain(name, null, valueCodeIsString, value);
	}

	private static DTEValue obtain(String name, String valueCode, boolean valueCodeIsString, Object value) {
		return new DTEValue(name, valueCode, valueCodeIsString, value);
//		return ContextSession.registerItem(new DTEValue(name, valueCode, valueCodeIsString, value));
	}

	@Override
	public void load(DTEntity entity) {
		var dv = TypeUtil.as(entity, DTEValue.class);
		if (dv == null)
			throw new IllegalArgumentException(strings("codeart", "TypeMismatch"));
		this._valueCode = dv._valueCode;
		this._valueCodeIsString = dv._valueCodeIsString;
		this._value = dv._value;
	}

}
