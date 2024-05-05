package apros.codeart.ddd.metadata;

import static apros.codeart.runtime.Util.propagate;

import apros.codeart.bytecode.ClassGenerator;
import apros.codeart.ddd.dynamic.DynamicEntity;
import apros.codeart.ddd.dynamic.DynamicObject;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;

final class SchemeCodeParser {

	private SchemeCodeParser() {
	}

	public static Class<?> generate(DTObject scheme) {

		var typeName = scheme.getString("name");
		var category = DomainObjectCategory.valueOf(scheme.getByte("category"));
		var superClass = getDomainClass(category);

		try (var cg = ClassGenerator.define(typeName, superClass)) {

			generateDefaultConstructor(cg);

			generateEmptyConstructor(cg);

//			DomainProperty.register(null, null, null)

			return cg.toClass();

		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private static void generateDefaultConstructor(ClassGenerator cg) {
		var g = cg.definePublicConstructor();
		g.invokeSuper(); // 执行super();
		// 执行 this.onConstructed();
		g.invoke(() -> {
			g.loadThis();
		}, "onConstructed", null);

		// 打上标签
		g.addAnnotation(ConstructorRepository.class);

// 		示例代码：
//		@ConstructorRepository
//		User() {
//			super();
//			this.onConstructed();
//		}

	}

	private static void generateEmptyConstructor(ClassGenerator cg) {
		var g = cg.definePublicConstructor((args) -> {
			args.add("isEmpty", boolean.class);
		});
		g.invokeSuper(() -> {
			g.loadVariable("isEmpty");
		}); // 执行super(isEmpty);
		// 执行 this.onConstructed();
		g.invoke(() -> {
			g.loadThis();
		}, "onConstructed", null);

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
		throw new IllegalArgumentException(Language.strings("codeart.ddd", "NoDomainClass", category.value()));
	}

}
