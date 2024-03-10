package com.apros.codeart.dto.serialization;

import java.lang.reflect.Field;

import com.apros.codeart.bytecode.LogicOperator;
import com.apros.codeart.bytecode.MethodGenerator;
import com.apros.codeart.runtime.TypeUtil;

class ArraySerializationInfo extends MemberSerializationInfo {

	public ArraySerializationInfo(Field field, DTOMemberAnnotation memberAnn) {
		super(field, memberAnn);
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

			var elementType = TypeUtil.resolveElementType(this.getTargetClass());
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

				var elementType = TypeUtil.resolveElementType(this.getTargetClass());

				g.each(() -> {
					loadMemberValue(g);
				}, elementType, (item) -> {
					SerializationMethodHelper.writeElement(g, this.getDTOMemberName(), elementType, () -> {
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
					return LogicOperator.LessThan;
				}, () -> {
//数量小于1
//array = new array[];

					var elementType = TypeUtil.resolveElementType(this.getTargetClass());
					g.assign(array, () -> {
						g.newArray(elementType, () -> {
							g.load(length);
						});
					});
				}, () -> {

					var elementType = TypeUtil.resolveElementType(this.getTargetClass());

//int[] = new int[c];
					g.assign(array, () -> {
						g.newArray(elementType, () -> {
							g.load(length);
						});
					});

					g.loop(length, (index) -> {
						var item = g.declare(elementType);

						g.assign(item, () -> {
							SerializationMethodHelper.readElement(g, this.getDTOMemberName(), elementType, index);
						});

						g.saveElement(array, index, item);
					});

				});
			}

			g.load(array);

		});
	}
}
