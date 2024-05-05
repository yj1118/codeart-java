package apros.codeart.bytecode;

import java.util.function.Consumer;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

public final class FieldGenerator implements AutoCloseable {

	private final FieldVisitor _visitor;

	public FieldGenerator(FieldVisitor visitor) {
		_visitor = visitor;
	}

	/**
	 * 
	 * 添加无参注解
	 * 
	 * @param annClass
	 */
	public void addAnnotation(Class<?> annClass) {
		addAnnotation(annClass, null);
	}

	public void addAnnotation(Class<?> annClass, Consumer<AnnotationOperation> fill) {
		String desc = Type.getDescriptor(annClass);
		var ag = _visitor.visitAnnotation(desc, true);
		if (fill != null)
			fill.accept(new AnnotationOperation(ag));
		ag.visitEnd();
	}

	@Override
	public void close() {
		_visitor.visitEnd();
	}

}
