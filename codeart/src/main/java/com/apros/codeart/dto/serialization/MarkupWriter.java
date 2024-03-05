package com.apros.codeart.dto.serialization;

import com.apros.codeart.dto.DTObject;
import com.apros.codeart.runtime.TypeUtil;

class MarkupWriter extends DTOWriter {

	private DTObject _dto;

	public MarkupWriter(DTObject dto) {
		_dto = dto;
	}

	// public override
//
//	void WriteElement<T>(
//	string name, bool elementIsPrimitive,
//	T element)
//	{
//		if (elementIsPrimitive) {
//			var elementDTO = _dto.CreateAndPush(name);
//			elementDTO.SetValue(element);
//		} else {
//			var elementDTO = DTObjectSerializer.Instance.Serialize(element);
//			_dto.Push(name, elementDTO);
//		}
//
//	}

	@Override
	public void write(String name, Object value) {
		if (value == null)
			return; // 为isNull的成员不输出, 这里有可能要升级是否为null的算法,todo
		// 是否自定义
		var serializable = TypeUtil.as(value, IDTOSerializable.class);
		if (serializable != null) {
			serializable.serialize(_dto, name);
			return;
		}

		var obj = DTObjectSerializer.Instance.serialize(value);
		_dto.setObject(name, obj);
	}
}
