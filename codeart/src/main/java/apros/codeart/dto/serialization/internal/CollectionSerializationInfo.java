package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Field;

import apros.codeart.bytecode.LogicOperator;
import apros.codeart.bytecode.MethodGenerator;
import apros.codeart.runtime.FieldUtil;

/**
 * java的泛型集合不可能是基元类型，所以只用考虑引用类型即可
 */
class CollectionSerializationInfo extends MemberSerializationInfo {

	private Class<?> _elementType;

	public Class<?> elementType() {
		return _elementType;
	}

	private Class<?> getElementType(Field field) {
		var atas = FieldUtil.getActualTypeArguments(field);
		if (atas.length == 0)
			return Object.class;

		return atas[0]; // 将第0个泛型参数作为集合的成员类型
	}

	public CollectionSerializationInfo(Field field, DTOMemberImpl memberAnn) {
		super(field, memberAnn);
		_elementType = getElementType(field);
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

			//// 写入数组
			SerializationMethodHelper.writeArray(g, this.getDTOMemberName());

			// 写入每个项
			g.each(() -> {
				loadMemberValue(g);
			}, _elementType, item -> {
				SerializationMethodHelper.writeElement(g, this.getDTOMemberName(), () -> {
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

			var targetClass = this.getTargetClass();

			var list = g.declare(targetClass);

			g.when(() -> {
				g.load(count);
				g.load(0);
				return LogicOperator.AreEqual;
			}, () -> {
//数量小于1
//list = new List<T>();
//				var elementType = this.TargetType.ResolveElementType();
				g.assign(list, () -> {
					g.newObject(targetClass);
				});
			}, () -> {
//list = new List<T>();
				g.assign(list, () -> {
					g.newObject(targetClass);
				});

				g.loopLength(count, (index) -> {
					var item = g.declare(_elementType);

					g.assign(item, () -> {
						SerializationMethodHelper.readElement(g, this.getDTOMemberName(), index, _elementType);
					});

					g.invoke(list, "add", () -> {
						g.load(item);
					}, true);
				});

//				g.loop(list, (item, index, length) -> {
//
//					g.assign(item, () -> {
//						SerializationMethodHelper.readElement(g, this.getDTOMemberName(), index, _elementType);
//					});
//
//					g.invoke(list, "add", () -> {
//						g.load(item);
//					}, true);
//				}, _elementType);
			});

			g.load(list);

		});
	}
}
