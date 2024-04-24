package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Function;

import apros.codeart.dto.DTObject;
import apros.codeart.runtime.FieldUtil;
import apros.codeart.util.LazyIndexer;

public class TypeSchemaCodeInfo extends TypeSerializationInfo {
	private SchemaCodes _schemaCodes;

	public TypeSchemaCodeInfo(Class<?> classType, String schemaCode) {
		super(classType);
		_schemaCodes = new SchemaCodes(classType, schemaCode);
		this.initialize();
	}

	protected DTOClassImpl getClassAnnotation(Class<?> classType) {
		return DTOClassImpl.Default;
	}

	@Override
	protected DTOMemberImpl getMemberAnnotation(Field field) {
		var name = FieldUtil.getAgreeName(field);
		if (_schemaCodes.canMarkup(name))
			return new DTOMemberImpl(name);
		return null;
	}

	@Override
	protected void onBuildMembers(ArrayList<MemberSerializationInfo> members) {
		_schemaCodes.sort(members);
	}

	@Override
	public void serialize(Object instance, DTObject dto) {
		SchemaCodeWriter writer = new SchemaCodeWriter(dto, _schemaCodes);
		getSerializeMethod().invoke(instance, writer);
	}

	@Override
	public void deserialize(Object instance, DTObject dto) {
		SchemaCodeReader reader = new SchemaCodeReader(dto, _schemaCodes);

		getDeserializeMethod().invoke(instance, reader);
	}

	private static final Function<Class<?>, Function<String, TypeSchemaCodeInfo>> _getTypeInfo = LazyIndexer
			.<Class<?>, Function<String, TypeSchemaCodeInfo>>init((classType) -> {
				return LazyIndexer.<String, TypeSchemaCodeInfo>init((schemaCode) -> {
					return new TypeSchemaCodeInfo(classType, schemaCode);
				});
			});

	public static TypeSchemaCodeInfo getTypeInfo(Class<?> classType, String schemaCode) {
		return _getTypeInfo.apply(classType).apply(schemaCode);
	}
}
