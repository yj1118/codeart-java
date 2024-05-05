package apros.codeart.bytecode;

import org.objectweb.asm.AnnotationVisitor;

public class AnnotationOperation {
	private AnnotationVisitor _av;

	AnnotationOperation(AnnotationVisitor av) {
		_av = av;
	}

	/**
	 * 
	 * 为注解添加参数
	 * 
	 * @param name
	 * @param value
	 */
	public void add(String name, Object value) {
		_av.visit(name, value);
	}
}
