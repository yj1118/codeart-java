package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Function;

import apros.codeart.dto.DTObject;
import apros.codeart.util.LazyIndexer;

class TypeMakupInfo extends TypeSerializationInfo {

	public TypeMakupInfo(Class<?> classType) {
		super(classType);
		this.initialize();
	}

	@Override
	protected DTOClassImpl getClassAnnotation(Class<?> classType) {
		return DTOClassImpl.get(classType);
	}

	@Override
	protected DTOMemberImpl getMemberAnnotation(Field field) {
		return DTOMemberImpl.get(field);
	}

	@Override
	protected void onBuildMembers(ArrayList<MemberSerializationInfo> members) {
		// 什么都不用执行
	}

	@Override
	public void serialize(Object instance, DTObject dto) {
		var writer = new MarkupWriter(dto);
		getSerializeMethod().invoke(instance, writer);
	}

	@Override
	public void deserialize(Object instance, DTObject dto) {
		var reader = new MarkupReader(dto);
		getDeserializeMethod().invoke(instance, reader);
	}

	private static final Function<Class<?>, TypeMakupInfo> _getTypeInfo = LazyIndexer.init(classType -> {
		return new TypeMakupInfo(classType);
	});

	public static TypeMakupInfo getTypeInfo(Class<?> classType) {
		return _getTypeInfo.apply(classType);
	}

}
