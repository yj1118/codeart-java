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

	@Override
	public void generateDeserializeIL(MethodGenerator g) {
		setMember(g, () -> {
			var count = g.declare(int.class);
			SerializationMethodHelper.readLength(g, this.getDTOMemberName());// 读取数量
			count.save();

			var list = g.declare(this.getTargetClass());

			g.when(() -> {
				g.load(count);
				g.load(0);
				return LogicOperator.LessThan;
			}, () -> {
//数量小于1
//list = new List<T>();
//				var elementType = this.TargetType.ResolveElementType();
				g.assign(list, () -> {
					g.newObject(this.getTargetClass());
				});
			}, () -> {
//list = new List<T>();
				g.assign(list, () -> {
					g.newObject(this.getTargetClass());
				});

//				var elementType = this.TargetType.ResolveElementType();

//				g.For(count, (index) -> {
//					var item = g.declare(elementType);
//
//					g.assign(item, () -> {
//						SerializationMethodHelper.readElement(g, this.DTOMemberName, elementType, index);
//					});
//
//					g.Load(list);
//					g.Load(item);
//					g.Call(this.TargetType.ResolveMethod("add", elementType));
//				});
			});

			g.load(list);

		});
	}
}
