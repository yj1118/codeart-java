package com.apros.codeart.dto.serialization;

import com.apros.codeart.dto.DTObject;
import com.apros.codeart.runtime.TypeUtil;

class SchemaCodeWriter extends DTOWriter {

	/// <summary>
	/// 架构代码
	/// </summary>
	private SchemaCodes _schemaCodes;

	public SchemaCodeWriter(DTObject dto, SchemaCodes schemaCodes) {
		super(dto);
		_schemaCodes = schemaCodes;
	}

//	public override
//
//	void WriteElement<T>(
//	string name, bool elementIsPrimitive,
//	T element)
//	{
//	    if (elementIsPrimitive)
//	    {
//	        var elementDTO = _dto.CreateAndPush(name);
//	        elementDTO.SetValue(element);
//	    }
//	    else
//	    {
//	        var elementType = typeof(T);
//	        if(elementType.IsEnum)
//	        {
//	            var underlyingType = Enum.GetUnderlyingType(elementType);
//	            var value = DataUtil.ToValue(element, underlyingType);
//	            var elementDTO = _dto.CreateAndPush(name);
//	            elementDTO.SetValue(value);
//	        }
//	        else
//	        {
//	            string schemaCode = _schemaCodes.GetSchemaCode(name, () => typeof(T));
//	            var elementDTO = DTObjectMapper.Instance.Load(schemaCode, element);
//	            _dto.Push(name, elementDTO);
//	        }
//	    }
//	}

	@Override
	public void writeObject(String name, Object value) {
		if (value == null)
			return; // 为isNull的成员不输出
		// 是否自定义
		var serializable = TypeUtil.as(value, IDTOSerializable.class);
		if (serializable != null) {
			serializable.serialize(_dto, name);
			return;
		}
		String schemaCode = _schemaCodes.getSchemaCode(name, () -> value.getClass());
		var dtoValue = DTObjectMapper.load(schemaCode, value);
		_dto.setObject(name, dtoValue);
	}
}
