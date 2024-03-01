package com.apros.codeart.dto.serialization;

import java.lang.reflect.Field;

import com.apros.codeart.bytecode.LogicOperator;
import com.apros.codeart.bytecode.MethodGenerator;

/**
 * java的泛型集合不可能是基元类型，所以只用考虑引用类型即可
 */
class CollectionSerializationInfo extends MemberSerializationInfo {
	public CollectionSerializationInfo(Field field, DTOMemberAnnotation memberAnn) {
		super(field, memberAnn);
	}

	public CollectionSerializationInfo(Class<?> classType) {
		super(classType);
	}

	@Override
	public void generateSerializeIL(MethodGenerator g) {
		g.when(() -> {
			loadMemberValue(g);// 加载集合到堆栈上，检查是否为null
			return LogicOperator.IsNull;
		}, () -> {
			SerializationMethodHelper.writeArray(g, this.getDTOMemberName());
		}, () -> {
//			var elementType = this.getTargetClass().ResolveElementType();
			var elementType = Object.class; // java的泛型集合不可能是基元类型，所以只用考虑引用类型即可
			//// 写入数组
			SerializationMethodHelper.writeArray(g, this.getDTOMemberName());

			// 写入每个项
			g.each(() -> {
				loadMemberValue(g);
			}, elementType, item -> {
				SerializationMethodHelper.writeElement(g, this.getDTOMemberName(), elementType, () -> {
					item.load();
				});
			});
		});
	}

//	@Override
//public void generateDeserializeIL(MethodGenerator g)
//{
//SetMember(g, () ->
//{
//var count = g.Declare<int>();
//g.Assign(count, () ->
//{
//SerializationMethodHelper.ReadLength(g, this.DTOMemberName);//读取数量
//});
//
//var list = g.Declare(this.TargetType);
//
//g.If(() =>
//{
//g.Load(count);
//g.Load(0);
//return LogicOperator.LessThan;
//}, () =>
//{
////数量小于1
////list = new List<T>();
//var elementType = this.TargetType.ResolveElementType();
//g.Assign(list, () =>
//{
//g.NewObject(this.TargetType);
//});
//}, () =>
//{
////list = new List<T>();
//g.Assign(list, () =>
//{
//g.NewObject(this.TargetType);
//});
//
//var elementType = this.TargetType.ResolveElementType();
//
//g.For(count, (index) =>
//{
//var item = g.Declare(elementType);
//
//g.Assign(item, () =>
//{
//SerializationMethodHelper.ReadElement(g, this.DTOMemberName, elementType, index);
//});
//
//g.Load(list);
//g.Load(item);
//g.Call(this.TargetType.ResolveMethod("Add", elementType));
//});
//});
//
//g.Load(list);
//
//});
//}
}
