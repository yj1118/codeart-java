package apros.codeart.dto.serialization.internal;

import apros.codeart.dto.DTObject;
import apros.codeart.dto.serialization.IDTOSerializable;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.Common;

class MarkupWriter extends DTOWriter {

	public MarkupWriter(DTObject dto) {
		super(dto);
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
	public void writeObject(String name, Object value) {
		if (Common.isNull(value))
			return; // 为isNull的成员不输出, 这里有可能要升级是否为null的算法,todo
		// 是否自定义
		var serializable = TypeUtil.as(value, IDTOSerializable.class);
		if (serializable != null) {
			serializable.serialize(_dto, name);
			return;
		}

		var obj = DTObjectMapper.load(value);
		_dto.setObject(name, obj);
	}
}
