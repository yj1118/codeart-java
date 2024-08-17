package apros.codeart.bytecode;

import static apros.codeart.runtime.Util.propagate;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

import apros.codeart.util.ListUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import apros.codeart.util.Guid;

public final class ClassGenerator implements AutoCloseable {
    private final ClassWriter _cw;

    private boolean _closed = false;
    private final String _className;

    public String getClassName() {
        return _className;
    }

    private final Class<?> _superClass;

    public Class<?> superClass() {
        return _superClass;
    }

    private final String _superClassName;

    public String getSuperClassName() {
        return _superClassName;
    }

    private ClassGenerator(int access, String className, Class<?> superClass) {
        _cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        _superClass = superClass;
        _superClassName = Type.getInternalName(superClass);
//		String.format("com/apros/codeart/runtime/%s", className)
        _cw.visit(Opcodes.V1_8, access, className, null, _superClassName, null);
        _className = className;
    }

    // region

    private static String randomClassName() {
        String guid = Guid.compact();
        return String.format("DynamicClass_%s", guid);
    }

    public static ClassGenerator define(Class<?> superClass) {
        return new ClassGenerator(Opcodes.ACC_PUBLIC, randomClassName(), superClass);
    }

    public static ClassGenerator define() {
        return new ClassGenerator(Opcodes.ACC_PUBLIC, randomClassName(), Object.class);
    }

    public static ClassGenerator define(String className) {
        return new ClassGenerator(Opcodes.ACC_PUBLIC, className, Object.class);
    }

    public static ClassGenerator define(String className, Class<?> superClass) {
        return new ClassGenerator(Opcodes.ACC_PUBLIC, className, superClass);
    }

    // endregion

    /**
     * 定义一个方法
     */
    public MethodGenerator defineMethod(final int access, final String name, final Class<?> returnClass,
                                        Consumer<IArgumenter> getArgs) {

        ArrayList<MethodParameter> args = new ArrayList<MethodParameter>(5);

        if (getArgs != null) {
            getArgs.accept((n, t) -> {
                args.add(new MethodParameter(n, t));
            });
        }

        Type[] types = new Type[args.size()];

        for (int i = 0; i < args.size(); i++) {
            types[i] = Type.getType(args.get(i).getType());
        }

        String descriptor = Type.getMethodDescriptor(Type.getType(returnClass), types);
        MethodVisitor visitor = _cw.visitMethod(access, name, descriptor, null, null);

        return new MethodGenerator(this, visitor, access, new ClassWrapper(returnClass), args);
    }

    public MethodGenerator defineMethodPublic(boolean isStatic, final String name, final Class<?> returnClass,
                                              Consumer<IArgumenter> getArgs) {

        int access = Opcodes.ACC_PUBLIC;
        if (isStatic)
            access += Opcodes.ACC_STATIC;

        return defineMethod(access, name, returnClass, getArgs);
    }

    public MethodGenerator defineMethodPublicStatic(final String name, final Class<?> returnClass) {

        return defineMethodPublic(true, name, returnClass, null);
    }

    public MethodGenerator defineMethodPublicStatic(final String name, final Class<?> returnClass,
                                                    Consumer<IArgumenter> getArgs) {

        return defineMethodPublic(true, name, returnClass, getArgs);
    }

    //region 字符串为返回值的版本

    public MethodGenerator defineMethodPublicStatic(final String name, final String returnClassName) {

        return defineMethodPublic(true, name, returnClassName);
    }

    public MethodGenerator defineMethodPublic(boolean isStatic, final String name, final String returnClassName) {

        int access = Opcodes.ACC_PUBLIC;
        if (isStatic)
            access += Opcodes.ACC_STATIC;

        return defineMethod(access, name, returnClassName);
    }

    public MethodGenerator defineMethod(final int access, final String name, final String returnClassName) {

        String descriptor = String.format("()L%s;", returnClassName);
        MethodVisitor visitor = _cw.visitMethod(access, name, descriptor, null, null);

        return new MethodGenerator(this, visitor, access, new ClassWrapper(returnClassName), ListUtil.empty());
    }

    //endregion

    /**
     * 定义一个构造函数
     */
    public MethodGenerator defineConstructor(final int access, Consumer<IArgumenter> getArgs) {
        return defineMethod(access, "<init>", void.class, getArgs);
    }

    public MethodGenerator definePublicConstructor(Consumer<IArgumenter> getArgs) {
        return defineConstructor(Opcodes.ACC_PUBLIC, getArgs);
    }

    /**
     * 静态构造，注意该方法只能执行一次
     *
     * @return
     */
    public MethodGenerator defineStaticConstructor() {
        return defineMethod(Opcodes.ACC_STATIC, "<clinit>", void.class, null);
    }

    /**
     * 定义一个无参的公开的构造函数
     *
     * @return
     */
    public MethodGenerator definePublicConstructor() {
        return defineConstructor(Opcodes.ACC_PUBLIC, null);
    }

    public FieldGenerator defineStaticFinalField(String name, Class<?> fieldType) {
        return defineField(true, true, true, name, Type.getDescriptor(fieldType));
    }

    /**
     * 创建一个字段，该字段的类型是由字符串指定
     *
     * @param name
     */
    public FieldGenerator defineStaticFinalField(String name, String typeName) {
        return defineField(true, true, true, name, String.format("L%s;", typeName));
    }

    public FieldGenerator definePrivateStaticFinalField(String name, String typeName) {
        return defineField(false, true, true, name, String.format("L%s;", typeName));
    }

    public FieldGenerator defineField(boolean isPublic, boolean isStatic, boolean isFinal, String name,
                                      String fieldType) {
        int access = 0;
        if (isPublic)
            access += Opcodes.ACC_PUBLIC;
        else
            access += Opcodes.ACC_PRIVATE;

        if (isStatic)
            access += Opcodes.ACC_STATIC;

        if (isFinal)
            access += Opcodes.ACC_FINAL;

        var visitor = _cw.visitField(access, name, fieldType, null, null);
        return new FieldGenerator(visitor);
    }

    public void save() {
        String filePath = _className.replace('.', '/') + ".class";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // 写入字节码到文件
            fos.write(this.toBytes());
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    public byte[] toBytes() {
        this.close();
        return _cw.toByteArray();
    }

    public Class<?> toClass() {
        try {
            byte[] bytes = this.toBytes();
            ClassLoaderImpl classLoader = new ClassLoaderImpl(bytes);
            return classLoader.loadClass(_className);
        } catch (Throwable e) {
            throw propagate(e);
        }
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

        private final byte[] _classData;

        public ClassLoaderImpl(byte[] classData) {
            _classData = classData;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return defineClass(name, _classData, 0, _classData.length);
        }
    }
}
