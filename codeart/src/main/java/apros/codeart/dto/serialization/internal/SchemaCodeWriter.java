package apros.codeart.dto.serialization.internal;

import apros.codeart.dto.DTObject;
import apros.codeart.dto.serialization.IDTOSerializable;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.Common;

class SchemaCodeWriter extends DTOWriter {

    /// <summary>
    /// 架构代码
    /// </summary>
    private final SchemaCodes _schemaCodes;

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
        if (Common.isNull(value))
            return; // 为isNull的成员不输出

        String schemaCode = _schemaCodes.getSchemaCode(name, value::getClass);

        // 是否自定义
        var serializable = TypeUtil.as(value, IDTOSerializable.class);
        if (serializable != null) {
            var dtoValue = serializable.getData(schemaCode);
            _dto.setObject(name, dtoValue);
        } else {
            var dtoValue = DTObjectMapper.load(schemaCode, value);
            _dto.setObject(name, dtoValue);
        }
    }
}
