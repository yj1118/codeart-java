package com.apros.codeart.dto.serialization;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Function;

import com.apros.codeart.dto.DTObject;
import com.apros.codeart.util.LazyIndexer;

class TypeMakupInfo extends TypeSerializationInfo {
	public TypeMakupInfo(Class<?> classType) {
		super(classType);
		this.initialize();
	}

	@Override
	protected DTOClassAnnotation getClassAnnotation(Class<?> classType) {
		return DTOClassAnnotation.get(classType);
	}

	@Override
	protected DTOMemberAnnotation getMemberAnnotation(Field field) {
		return DTOMemberAnnotation.get(field);
	}

	@Override
	protected void onBuildMembers(ArrayList<MemberSerializationInfo> members) {
		// 什么都不用执行
	}

	@Override
	public void serialize(Object instance, DTObject dto) {
		var writer = new MarkupWriter();
		writer.Initialize(dto);
		SerializeMethod(instance, writer);
	}

	@Override
	public void deserialize(Object instance, DTObject dto) {
		var reader = new MarkupReader();
		reader.initialize(dto);
		DeserializeMethod(instance, reader);
	}

	private static final Function<Class<?>, TypeMakupInfo> _getTypeInfo = LazyIndexer.init(classType -> {
		return new TypeMakupInfo(classType);
	});

	public static TypeMakupInfo GetTypeInfo(Class<?> classType) {
		return _getTypeInfo(classType);
	}

}
