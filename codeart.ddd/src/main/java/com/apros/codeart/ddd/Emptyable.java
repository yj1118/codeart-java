package com.apros.codeart.ddd;

import java.util.Optional;

import com.apros.codeart.dto.DTObject;
import com.apros.codeart.dto.serialization.IDTOSerializable;
import com.apros.codeart.util.INullProxy;
import com.apros.codeart.util.StringUtil;

/**
 * 不要改动设计，确保是不可变的，这就是值类型的数值
 * 
 * @param <T>
 */
public class Emptyable<T> implements IEmptyable, IDTOSerializable, INullProxy {

	private Optional<T> _value;

	public T value() {
		return _value.get();
	}

	public Object getValue() {
		if (this.isEmpty())
			return null;
		return this.value();
	}

	public boolean isEmpty() {
		return _value.isEmpty();
	}

	public boolean isNull() {
		return this.isEmpty();
	}

	public Emptyable(T value) {
		_value = Optional.ofNullable(value);
	}

	/// <summary>
	/// 将自身的内容序列化到<paramref name="owner"/>中，
	/// </summary>
	/// <param name="owner"></param>
	/// <param name="name">目标对象作为成员的名称</param>
	@Override
	public void serialize(DTObject owner, String name) {
		if (this.isEmpty())
			return;
		owner.setValue(name, this.getValue());
	}

	@Override
	public DTObject getData() {
		if (this.isEmpty())
			return DTObject.Empty;
		DTObject dto = DTObject.editable();
		dto.setValue(this.getValue());
		return dto;
	}

	@Override
	public String toString() {
		if (_value.isEmpty())
			return StringUtil.empty();
		return _value.get().toString();
	}

	public static <T> Emptyable<T> createEmpty() {
		return new Emptyable<T>(null);
	}
}
