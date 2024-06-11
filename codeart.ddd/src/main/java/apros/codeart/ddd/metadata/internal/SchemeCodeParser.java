package apros.codeart.ddd.metadata.internal;

import static apros.codeart.runtime.Util.propagate;

import apros.codeart.bytecode.ClassGenerator;
import apros.codeart.bytecode.FieldGenerator;
import apros.codeart.bytecode.MethodGenerator;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.dynamic.DynamicEntity;
import apros.codeart.ddd.dynamic.DynamicObject;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.metadata.DomainObjectCategory;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;

public final class SchemeCodeParser {

    private SchemeCodeParser() {
    }

    /*
     *
     * 在使用 ASM 生成字节码时，ASM 不会检查字段的类型描述符是否指向已存在的类。因此，你可以在生成类 A 的字节码时引用类 B，即使类 B
     * 的字节码尚未生成。
     *
     * 然而，在实际运行时加载类 A 或者使用类 A 的字段时，如果类 B 尚未被加载或者定义，就会抛出 NoClassDefFoundError 或者
     * ClassNotFoundException。因此，在生成字节码时，你需要确保所有的类型引用都是有效的，否则在运行时可能会出现问题。
     *
     *
     *
     *
     */

    @SuppressWarnings("unchecked")
    public static Class<? extends IDomainObject> generate(DTObject scheme) {

        var className = scheme.getString("name");
        var category = DomainObjectCategory.valueOf(scheme.getByte("category"));
        var superClass = getDomainClass(category);

        try (var cg = ClassGenerator.define(className, superClass)) {
            generateConstructor(cg);

            // 静态构造的方法
            // staticConstructorMethod
            try (var scm = cg.definePublicConstructor()) {
                generateEmpty(className, cg, scm);

                // var declaringType = Class.forName("User");
                scm.declare(Class.class, "declaringType");
                scm.assign("declaringType", () -> {
                    scm.classForName(className);
                });

                // 生成领域属性
                var props = scheme.getObjects("props", false);

                if (props != null) {

                    for (var prop : props) {
                        generateDomainProperty(prop, cg, scm);
                    }

                }
            }

            return (Class<? extends IDomainObject>) cg.toClass();

        } catch (Exception e) {
            throw propagate(e);
        }
    }

    private static void generateConstructor(ClassGenerator cg) {
        generateDefaultConstructor(cg);
        generateEmptyConstructor(cg);
    }

    private static void generateDefaultConstructor(ClassGenerator cg) {
        try (var g = cg.definePublicConstructor()) {
            // 打上标签
            g.addAnnotation(ConstructorRepository.class);

            g.invokeSuper(); // 执行super();
            // 执行 this.onConstructed();
            g.invoke(() -> {
                g.loadThis();
            }, "onConstructed", null);

        }

// 		示例代码：
//		@ConstructorRepository
//		User() {
//			super();
//			this.onConstructed();
//		}

    }

    private static void generateEmptyConstructor(ClassGenerator cg) {
        try (var g = cg.definePublicConstructor((args) -> {
            args.add("isEmpty", boolean.class);
        })) {
            g.invokeSuper(() -> {
                g.loadVariable("isEmpty");
            }); // 执行super(isEmpty);
            // 执行 this.onConstructed();
            g.invoke(() -> {
                g.loadThis();
            }, "onConstructed", null);
        }

// 		示例代码：
//		User(boolean isEmpty) {
//			super(isEmpty);
//			this.onConstructed();
//		}

    }

    private static Class<?> getDomainClass(DomainObjectCategory category) {
        switch (category) {
            case DomainObjectCategory.AggregateRoot:
                return DynamicRoot.class;
            case DomainObjectCategory.EntityObject:
                return DynamicEntity.class;
            case DomainObjectCategory.ValueObject:
                return DynamicObject.class;
        }
        throw new IllegalArgumentException(Language.strings("apros.codeart.ddd", "NoDomainClass", category.value()));
    }

    private static void generateEmpty(String className, ClassGenerator cg, MethodGenerator scm) {
        try (var fg = cg.definePrivateStaticFinalField("EmptyInstance", className)) {
        }

        scm.assignStaticField("EmptyInstance", className, () -> {
            scm.newObject(className, () -> {
                scm.load(true);
            });
        });

		
        try (var fg = cg.defineMethodPublicStatic("empty", className)) {
            fg.loadStaticField(className, "EmptyInstance", className);
        }


        // 示例代码
        // private static final User EmptyInstance = new User(true);
        // public static User empty() {
        //		return EmptyInstance;
        //	}

    }

    private static void generateDomainProperty(DTObject property, ClassGenerator cg, MethodGenerator scm) {

        try {

            var propertyName = property.getString("name");
            var category = DomainPropertyCategory.valueOf(property.getByte("category"));
            var monotype = property.getString("monotype");
            var lazy = property.getBoolean("lazy");

            try (var fg = cg.defineStaticFinalField(propertyName, DomainProperty.class)) {
                // 为领域属性打上标签
                if (lazy) {
                    fg.addAnnotation(PropertyRepository.class, (ag) -> {
                        ag.add("lazy", true);
                    });
                }
                appendValidators(property, fg);
            }

            switch (category) {
                case DomainPropertyCategory.Primitive: {
                    var valueType = Class.forName(monotype);
                    scm.assignStaticField(propertyName, DomainProperty.class, () -> {
                        scm.invokeStatic(DomainProperty.class, "register", () -> {
                            scm.load(propertyName);
                            scm.load(valueType);
                            scm.loadVariable("declaringType");
                        });
                    });

                    // public static final DomainProperty TimeProperty =
                    // DomainProperty.register("time", DateTime.class, User.class);
                }
                break;
                case DomainPropertyCategory.PrimitiveList: {
                    var elementType = Class.forName(monotype);
                    scm.assignStaticField(propertyName, DomainProperty.class, () -> {
                        scm.invokeStatic(DomainProperty.class, "registerCollection", () -> {
                            scm.load(propertyName);
                            scm.load(elementType);
                            scm.loadVariable("declaringType");
                        });
                    });

                    // private static final DomainProperty TimesProperty =
                    // DomainProperty.registerCollection("time", DateTime.class,User.class);

                }
                break;
                case DomainPropertyCategory.ValueObject:
                case DomainPropertyCategory.AggregateRoot:
                case DomainPropertyCategory.EntityObject: {
                    var valueTypeName = monotype;
                    scm.assignStaticField(propertyName, DomainProperty.class, () -> {
                        scm.invokeStatic(DomainProperty.class, "register", () -> {
                            scm.load(propertyName);
                            scm.classForName(valueTypeName);
                            scm.loadVariable("declaringType");
                        });
                    });
                }
                break;
                case DomainPropertyCategory.ValueObjectList:
                case DomainPropertyCategory.EntityObjectList:
                case DomainPropertyCategory.AggregateRootList: {
                    var elementTypeName = monotype;
                    scm.assignStaticField(propertyName, DomainProperty.class, () -> {
                        scm.invokeStatic(DomainProperty.class, "registerCollection", () -> {
                            scm.load(propertyName);
                            scm.classForName(elementTypeName);
                            scm.loadVariable("declaringType");
                        });
                    });
                }
                break;
                default:
                    break;
            }
        } catch (ClassNotFoundException e) {
            throw propagate(e);
        }

    }

    private static void appendValidators(DTObject property, FieldGenerator fg) {

        var vals = property.getObjects("vals", false);
        if (vals == null)
            return;

        for (DTObject val : vals) {
            var name = val.getString("name");

            var valType = TypeUtil.getClass(name);
            if (valType == null)
                continue; // 没有找到验证器类型，证明是远程端自己定义的，不必理会

            if (val.exist("data")) {
                fg.addAnnotation(valType, (ag) -> {
                    val.each("data", (n, v) -> {
                        ag.add(n, v);
                    });
                });
            } else {
                fg.addAnnotation(valType);
            }

        }

    }

}
