package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Field;

import apros.codeart.bytecode.LogicOperator;
import apros.codeart.bytecode.MethodGenerator;

class ArraySerializationInfo extends MemberSerializationInfo {

	private Class<?> _elementType;

	public Class<?> elementType() {
		return _elementType;
	}

	public ArraySerializationInfo(Field field, DTOMemberImpl memberAnn) {
		super(field, memberAnn);
		_elementType = field.getType().getComponentType();
	}

	public ArraySerializationInfo(Class<?> classType) {
		super(classType);
	}

	@Override
	public void generateSerializeIL(MethodGenerator g) {

		var memberName = this.getDTOMemberName();

		g.when(() -> {
			loadMemberValue(g);// 加载集合到堆栈上，检查是否为null
			return LogicOperator.IsNull;
		}, () -> {

			SerializationMethodHelper.writeArray(g, memberName);
		}, () -> {
//先写入空数组
			SerializationMethodHelper.writeArray(g, memberName);

//写入每个项

			g.loop(() -> {
				loadMemberValue(g);
			}, (item, index, length) -> {
				SerializationMethodHelper.writeElement(g, memberName, () -> {
					g.load(item);
				});
			}, _elementType);

//			if (this.getMemberAnn().isBlob()) {
//				SerializationMethodHelper.writeBlob(g, this.getDTOMemberName(), () -> {
//					loadMemberValue(g);
//				});
//			} else {
//
////				var elementType = TypeUtil.resolveElementType(this.getTargetClass());
//
//				
//			}
		});
	}

	@Override
	public void generateDeserializeIL(MethodGenerator g) {
		setMember(g, () -> {

			var targetClass = this.getTargetClass();

			var array = g.declare(targetClass);
			var memberName = this.getDTOMemberName();

//			if (this.getMemberAnn().isBlob()) {
//				g.assign(array, () -> {
//					SerializationMethodHelper.readBlob(g, memberName);// 读取数量
//				});
//			} else {
//				
//			}

			var length = g.declare(int.class);
			g.assign(length, () -> {
				SerializationMethodHelper.readLength(g, memberName);// 读取数量
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
						SerializationMethodHelper.readElement(g, memberName, index, _elementType);
					});

					g.saveElement(array, index, item);
				});

			});

			g.load(array);

		});
	}
}
