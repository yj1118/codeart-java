package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

import apros.codeart.dto.serialization.DTOParameter;
import com.google.common.base.Preconditions;

import apros.codeart.dto.DTObject;
import apros.codeart.dto.serialization.IDTOSerializable;
import apros.codeart.runtime.Activator;
import apros.codeart.runtime.FieldUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.PrimitiveUtil;
import apros.codeart.util.StringUtil;

import static apros.codeart.i18n.Language.strings;

public abstract class TypeSerializationInfo {
    private DTOClassImpl _classAnn;

    public DTOClassImpl getClassAnn() {
        return _classAnn;
    }

    private final Class<?> _targetClass;

    /**
     * 被序列化的类型
     *
     * @return
     */
    public Class<?> getTargetClass() {
        return _targetClass;
    }

    private ArrayList<MemberSerializationInfo> _memberInfos;

    public Iterable<MemberSerializationInfo> getMemberInfos() {
        return _memberInfos;
    }

    /**
     * 是否为集合类，例如 array、collection、dictionary等
     *
     * @return
     */
    public boolean isCollection() {
        return TypeUtil.isCollection(_targetClass);
    }

    protected abstract DTOClassImpl getClassAnnotation(Class<?> classType);

    protected void initialize() {
        if (this._classAnn != null)
            return; // 已初始化
        this._classAnn = getClassAnnotation(_targetClass);
        if (_targetClass.isPrimitive() || _targetClass.equals(DTObject.class)) {
            // this.DTOSchemaCode = string.Empty;
        } else {
            _memberInfos = buildMembers();
            _serializeMethod = createSerializeMethod();
            _deserializeMethod = createDeserializeMethod();
            // this.DTOSchemaCode = "{}";
        }
    }

    protected abstract DTOMemberImpl getMemberAnnotation(Field field);

    private ArrayList<MemberSerializationInfo> buildMembers() {
        var memberInfos = new ArrayList<MemberSerializationInfo>();

        if (this.isCollection()) // 类型是集合，因此类型本身也要加入序列化
            memberInfos.add(MemberSerializationInfo.create(this._targetClass));

        var fields = FieldUtil.getFields(this.getTargetClass());
        for (var field : fields) {

            if (FieldUtil.isStatic(field))
                continue;

            var ann = getMemberAnnotation(field);
            if (ann == null)
                continue;

            memberInfos.add(MemberSerializationInfo.create(field, ann));
        }

        onBuildMembers(memberInfos);

        return memberInfos;
    }

    /**
     * 当构建成员信息完成时触发
     *
     * @param members
     */
    protected abstract void onBuildMembers(ArrayList<MemberSerializationInfo> members);

    protected TypeSerializationInfo(Class<?> classType) {
        Preconditions.checkNotNull(classType);
        Preconditions.checkArgument(!classType.isInterface()); // 确保不是接口
        Preconditions.checkArgument(!TypeUtil.isAbstract(classType)); // 确保不是抽象类
        _targetClass = classType;
    }

    private SerializeMethod _serializeMethod;

    public SerializeMethod getSerializeMethod() {
        return _serializeMethod;
    }

    private SerializeMethod createSerializeMethod() {
        return DTOSerializeMethodGenerator.generateMethod(this);
    }

    private DeserializeMethod _deserializeMethod;

    public DeserializeMethod getDeserializeMethod() {
        return _deserializeMethod;
    }

    private DeserializeMethod createDeserializeMethod() {
        return DTODeserializeMethodGenerator.generateMethod(this);
    }

    public DTObject serialize(String schemaCode, Object instance) {
        
        var serializable = TypeUtil.as(instance, IDTOSerializable.class);
        if (serializable != null) {
            return serializable.getData(schemaCode);
//            serializable.serialize(dto, StringUtil.empty()); // string.Empty意味着 序列化的内容会完全替换dto
        } else {
            var dto = DTObject.editable();
            serialize(instance, dto);
            return dto;
        }

    }

    /**
     * 将对象instance的信息序列化到dto里
     *
     * @param instance
     * @param dto
     */
    public abstract void serialize(Object instance, DTObject dto);

    public Object deserialize(DTObject dto) {
        var targetClass = this.getTargetClass();
        var ctors = _getConstructors.apply(_targetClass);
        var ctor = ctors.find(dto);
        if (ctor == null) {
            throw new NotFoundCtorException(targetClass);
        }

        if (ctor.parameters().isEmpty()) {
            var instance = Activator.createInstance(targetClass);
            deserialize(instance, dto);
            return instance;
        }


        var prms = ctor.parameters();
        var types = new Class<?>[1 + prms.size()];
        types[0] = targetClass;
        var args = new Object[prms.size()];

        for (var i = 0; i < prms.size(); i++) {
            var prm = prms.get(i);
            var type = prm.type();

            if (PrimitiveUtil.is(type)) {
                var value = prm.getValue(dto);
                args[i] = value;
            } else {
                var obj = prm.getObject(dto);
                var value = obj.save(type);
                args[i] = value;
            }
            types[i + 1] = type;
        }

        var instance = Activator.createInstance(types, args);
        deserialize(instance, dto);
        return instance;

    }

    private static record Constructors(Iterable<ConstructorInfo> constructors) {

        public ConstructorInfo find(DTObject dto) {
            // 已经根据参数的长度降序排序好了
            // 以下实现最多参数匹配到的构造函数返回
            for (var ctor : this.constructors()) {

                if (ctor.parameters().isEmpty()) return ctor;

                boolean finded = true;
                for (var p : ctor.parameters()) {
                    boolean match = false;
                    for (var name : p.names()) {
                        if (dto.exist(name)) {
                            match = true;
                            break;
                        }
                    }

                    if (!match) {
                        finded = false;
                        break;
                    }

                }
                if (finded)
                    return ctor;
            }
            return null;
        }

    }

    private static record ConstructorInfo(List<ParameterInfo> parameters) {

    }

    private static record ParameterInfo(Iterable<String> names, Class<?> type) {

        public Object getValue(DTObject dto) {
            for (var name : names) {
                if (dto.exist(name)) {
                    return dto.getValue(name);
                }
            }
            throw new IllegalStateException(strings("apros.codeart", "UnknownException"));
        }

        public DTObject getObject(DTObject dto) {
            for (var name : names) {
                if (dto.exist(name)) {
                    return dto.getObject(name);
                }
            }
            throw new IllegalStateException(strings("apros.codeart", "UnknownException"));
        }

    }

    private static final Function<Class<?>, Constructors> _getConstructors = LazyIndexer.init((instanceType) -> {
        var ctors = ListUtil.filter(instanceType.getConstructors(), (ctor) -> Modifier.isPublic(ctor.getModifiers()));

        ArrayList<ConstructorInfo> infos = new ArrayList<>();

        for (var ctor : ctors) {

            var prms = new ArrayList<ParameterInfo>();

            for (var p : ctor.getParameters()) {
                ArrayList<String> names = new ArrayList<>();
                names.add(p.getName());

                DTOParameter dtoParam = p.getAnnotation(DTOParameter.class);
                if (dtoParam != null) {
                    for (var t : dtoParam.value()) {
                        if (!ListUtil.containsIgnoreCase(names, t))
                            names.add(t);
                    }
                }

                names.trimToSize();
                prms.add(new ParameterInfo(names, p.getType()));
            }

            prms.trimToSize();
            infos.add(new ConstructorInfo(prms));
        }

        infos.trimToSize();

        infos.sort(new Comparator<ConstructorInfo>() {
            public int compare(ConstructorInfo c1, ConstructorInfo c2) {
                // 降序，让参数最多的在前面，为find算法提供支持
                return -Integer.compare(c1.parameters().size(), c2.parameters().size());
            }
        });
        return new Constructors(infos);
    });

    /**
     * 用 dto 里的数据，填充 instance 的属性
     *
     * @param instance
     * @param dto
     */
    public abstract void deserialize(Object instance, DTObject dto);

}
