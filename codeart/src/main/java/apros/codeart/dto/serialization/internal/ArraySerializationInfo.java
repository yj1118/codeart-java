package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Field;

import apros.codeart.bytecode.LogicOperator;
import apros.codeart.bytecode.MethodGenerator;
import apros.codeart.runtime.FieldUtil;

class ArraySerializationInfo extends MemberSerializationInfo {

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

	public ArraySerializationInfo(Field field, DTOMemberImpl memberAnn) {
		super(field, memberAnn);
		_elementType = getElementType(field);
	}

	public ArraySerializationInfo(Class<?> classType) {
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
//先写入空数组
			SerializationMethodHelper.writeArray(g, this.getDTOMemberName());

//写入每个项
			if (this.getMemberAnn().isBlob()) {
				SerializationMethodHelper.writeBlob(g, this.getDTOMemberName(), () -> {
					loadMemberValue(g);
				});
			} else {

//				var elementType = TypeUtil.resolveElementType(this.getTargetClass());

				var elementType = Object.class;

				g.each(() -> {
					loadMemberValue(g);
				}, elementType, (item) -> {
					SerializationMethodHelper.writeElement(g, this.getDTOMemberName(), () -> {
						g.load(item);
					});
				});
			}
		});
	}

	@Override
	public void generateDeserializeIL(MethodGenerator g) {
		setMember(g, () -> {
			var array = g.declare(this.getTargetClass());

			if (this.getMemberAnn().isBlob()) {
				g.assign(array, () -> {
					SerializationMethodHelper.readBlob(g, this.getDTOMemberName());// 读取数量
				});
			} else {
				var length = g.declare(int.class);
				g.assign(length, () -> {
					SerializationMethodHelper.readLength(g, this.getDTOMemberName());// 读取数量
				});

				g.when(() -> {
					g.load(length);
					g.load(0);
					return LogicOperator.AreEqual;
				}, () -> {
//数量小于1
//array = new array[];

					g.assign(array, () -> {
						g.newArray(_elementType, () -> {
							g.load(length);
						});
					});
				}, () -> {

//int[] = new int[c];
					g.assign(array, () -> {
						g.newArray(_elementType, () -> {
							g.load(length);
						});
					});

					g.loopLength(length, (index) -> {
						var item = g.declare(_elementType);

						g.assign(item, () -> {
							SerializationMethodHelper.readElement(g, this.getDTOMemberName(), index, _elementType);
						});

						g.saveElement(array, index, item);
					});

				});
			}

			g.load(array);

		});
	}
}
