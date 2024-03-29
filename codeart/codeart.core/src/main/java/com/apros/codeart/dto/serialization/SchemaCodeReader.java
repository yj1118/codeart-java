package com.apros.codeart.dto.serialization;

import com.apros.codeart.dto.DTObject;

class SchemaCodeReader extends DTOReader {

	/// <summary>
	/// 成员的架构代码
	/// </summary>
	private SchemaCodes _schemaCodes;

	public SchemaCodeReader(DTObject dto, SchemaCodes schemaCodes) {
		super(dto);
		_schemaCodes = schemaCodes;
	}

//	public override T ReadElement<T>(
//	string name,
//	int index)
//	{
//	     var dtoList = _dto.GetList(name, false);
//	     if (dtoList == null) return default(T);
//	     var dtoElement = dtoList[index];
//	     if (dtoElement.IsSingleValue) return dtoElement.GetValue<T>();
//
//	     var elementType = typeof(T);
//	     string schemaCode = _schemaCodes.GetSchemaCode(name, () => elementType);
//	     return (T)DTObjectMapper.Instance.Save(elementType, schemaCode, dtoElement);
//	 }

	@Override
	public Object readObject(Class<?> objectType, String name) {
		var dtoValue = _dto.getObject(name, null);
		if (dtoValue == null)
			return null;

		String schemaCode = _schemaCodes.getSchemaCode(name, () -> objectType);
		return DTObjectMapper.save(objectType, schemaCode, dtoValue);
	}
}
