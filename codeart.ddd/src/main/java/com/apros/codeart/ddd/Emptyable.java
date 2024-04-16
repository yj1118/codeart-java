package com.apros.codeart.ddd;

import static com.apros.codeart.i18n.Language.strings;

import java.time.LocalDateTime;
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
public abstract class Emptyable<T> implements IEmptyable, IDTOSerializable, INullProxy {

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

	public static Object createEmpty(Class<?> emptyableType) {

		if (emptyableType.equals(EmptyableDateTime.class))
			return EmptyableDateTime.Empty;

		if (emptyableType.equals(EmptyableInt.class))
			return EmptyableInt.Empty;

		if (emptyableType.equals(EmptyableLong.class))
			return EmptyableLong.Empty;

		throw new IllegalStateException(strings("codeart.ddd", "DidNotFindEmptyType", emptyableType.getName()));

	}

	public static IEmptyable create(Class<?> emptyableType, Object value) {

		if (emptyableType.equals(EmptyableDateTime.class))
			return new EmptyableDateTime((LocalDateTime) value);

		if (emptyableType.equals(EmptyableInt.class))
			return new EmptyableInt((Integer) value);

		if (emptyableType.equals(EmptyableLong.class))
			return new EmptyableLong((Long) value);

		throw new IllegalStateException(strings("codeart.ddd", "DidNotFindEmptyType", emptyableType.getName()));

	}

	public static Class<?> getValueType(Class<?> emptyableType) {
		if (emptyableType.equals(EmptyableDateTime.class))
			return EmptyableDateTime.ValueType;

		if (emptyableType.equals(EmptyableInt.class))
			return EmptyableInt.ValueType;

		if (emptyableType.equals(EmptyableLong.class))
			return EmptyableLong.ValueType;

		throw new IllegalStateException(strings("codeart.ddd", "DidNotFindValueType", emptyableType.getName()));
	}

}
