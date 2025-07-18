package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Field;

import apros.codeart.bytecode.LogicOperator;
import apros.codeart.bytecode.MethodGenerator;
import apros.codeart.dto.DTObject;
import apros.codeart.dto.DTObjects;
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

        if (field.getType().equals(DTObjects.class)) {
            return DTObject.class;
        }

        var args = FieldUtil.getActualTypeArguments(field);
        if (args.length == 0)
            return Object.class;

        return args[0]; // 将第0个泛型参数作为集合的成员类型
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
                }, _elementType);
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
                return LogicOperator.LessThan;
            }, () -> {
//数量小于1，直接返回null，只有等于0或者大于0才创建
                g.assign(list, () -> {
                    g.loadNull();
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
