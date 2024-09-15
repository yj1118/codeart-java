package apros.codeart.ddd.metadata.internal;

import static apros.codeart.runtime.Util.propagate;

import apros.codeart.bytecode.ClassGenerator;
import apros.codeart.bytecode.FieldGenerator;
import apros.codeart.bytecode.MethodGenerator;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.metadata.DomainObjectCategory;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.virtual.VirtualEntity;
import apros.codeart.ddd.virtual.VirtualObject;
import apros.codeart.ddd.virtual.VirtualRoot;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.PrimitiveUtil;
import apros.codeart.util.StringUtil;

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
            try (var scm = cg.defineStaticConstructor()) {
                generateEmpty(className, cg, scm);

                // 生成领域属性
                var props = scheme.getObjects("props", false);

                if (props != null) {

                    for (var prop : props) {
                        generateDomainProperty(prop, cg, scm);
                    }

                }
            }

            //cg.save();

            // 使用自定义类加载器加载生成的类
            return (Class<? extends IDomainObject>) cg.toClass(DomainObject.class.getClassLoader());

        } catch (Throwable e) {
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
            g.invokeThis("onConstructed", null, void.class);
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
            g.invokeThis("onConstructed", null, void.class);
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
                return VirtualRoot.class;
            case DomainObjectCategory.EntityObject:
                return VirtualEntity.class;
            case DomainObjectCategory.ValueObject:
                return VirtualObject.class;
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
            var propertyFieldName = String.format("%sProperty", propertyName);
            var category = DomainPropertyCategory.valueOf(property.getByte("category"));
            var monotype = property.getString("monotype");
            var lazy = property.getBoolean("lazy");

            try (var fg = cg.defineStaticFinalField(propertyFieldName, DomainProperty.class)) {
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
                    scm.assignStaticField(propertyFieldName, DomainProperty.class, () -> {
                        //不知道为什么，无法正常加载类似long.class的类型信息，所以用方法来加载了
                        String methodName = String.format("register%s", StringUtil.firstToUpper(monotype));

                        scm.invokeStatic(DomainProperty.class, methodName, () -> {
                            scm.load(propertyName);
                            scm.loadClass(cg.getClassName());
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
                            scm.loadClass(cg.getClassName());
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
                            scm.loadClass(valueTypeName);
                            scm.loadClass(cg.getClassName());
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
                            scm.loadClass(elementTypeName);
                            scm.loadClass(cg.getClassName());
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

            var valType = TypeUtil.getClass(String.format("apros.codeart.ddd.validation.%s", name));
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
