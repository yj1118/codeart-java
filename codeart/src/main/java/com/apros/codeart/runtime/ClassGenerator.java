package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.apros.codeart.util.StringUtil;

public final class ClassGenerator implements AutoCloseable {
	private ClassWriter _cw;

	private boolean _closed = false;
	private String _className;

	private ClassGenerator(int access, String className, Class<?> subClass) {
		_cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		var subClassName = Type.getInternalName(subClass);
		_cw.visit(Opcodes.V1_8, access, className, null,
				subClassName, null);
		_className = className;
	}

	// region

	private static String randomClassName() {
		String guid = StringUtil.uuid();
		return String.format("DynamicClass_%s", guid);
	}

	public static ClassGenerator define(Class<?> subClass) {
		return new ClassGenerator(Opcodes.ACC_PUBLIC, randomClassName(), subClass);
	}

	public static ClassGenerator define() {
		return new ClassGenerator(Opcodes.ACC_PUBLIC, randomClassName(), Object.class);
	}

	public static ClassGenerator define(String className) {
		return new ClassGenerator(Opcodes.ACC_PUBLIC, className, Object.class);
	}

	public static ClassGenerator define(String className, Class<?> subClass) {
		return new ClassGenerator(Opcodes.ACC_PUBLIC, className, subClass);
	}

	// endregion

	/**
	 * 定义一个方法
	 */
	public MethodGenerator defineMethod(final int access, final String name, final Class<?> returnClass,
			Consumer<IArgumenter> getArgs) {

		ArrayList<Argument> args = new ArrayList<Argument>(5);

		getArgs.accept((n, t) -> {
			args.add(new Argument(n, t));
		});

		Type[] types = new Type[args.size()];

		for (var i = 0; i < args.size(); i++) {
			types[i] = Type.getType(args.get(i).getType());
		}

		var descriptor = Type.getMethodDescriptor(Type.getType(returnClass), types);
		// 定义静态方法
		MethodVisitor visitor = _cw.visitMethod(access, name, descriptor, null, null);

		return new MethodGenerator(visitor, access, returnClass, args);
	}

	public MethodGenerator defineMethodPublic(boolean isStatic, final String name, final Class<?> returnClass,
			Consumer<IArgumenter> getArgs) {

		return defineMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, name, returnClass, getArgs);
	}

	public void save() {
		String filePath = _className.replace('.', '/') + ".class";
		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			// 写入字节码到文件
			fos.write(this.toBytes());
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	public byte[] toBytes() {
		this.close();
		return _cw.toByteArray();
	}

	public Class<?> toClass() {
		var bytes = this.toBytes();
		var classLoader = new ClassLoaderImpl();
//		var l = Thread.currentThread().getContextClassLoader();
		return classLoader.loadClass(_className, bytes);
	}

	@Override
	public void close() {
		// 类生成完成
		if (_closed)
			return;
		_closed = true;
		_cw.visitEnd();
	}

	// 自定义类加载器
	private static class ClassLoaderImpl extends ClassLoader {

		public Class<?> loadClass(String name, byte[] bytecode) {
			return defineClass(name, bytecode, 0, bytecode.length);
		}
	}

}
