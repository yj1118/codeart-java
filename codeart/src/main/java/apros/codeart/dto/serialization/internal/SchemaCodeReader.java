package apros.codeart.dto.serialization.internal;

import apros.codeart.dto.DTObject;

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
	public Object readElement(String name, int index, Class<?> elementType) {
		var e = _dto.getElement(name, index, false);
		if (e == null)
			return null;

		if (e.isSingleValue())
			return e.getValue();

		String schemaCode = _schemaCodes.getSchemaCode(name, () -> elementType);

		return e.save(elementType, schemaCode);
	}

	@Override
	public Object readObject(Class<?> objectType, String name) {
		var dtoValue = _dto.getObject(name, null);
		if (dtoValue == null)
			return null;

		String schemaCode = _schemaCodes.getSchemaCode(name, () -> objectType);
		return DTObjectMapper.save(objectType, schemaCode, dtoValue);
	}
}
