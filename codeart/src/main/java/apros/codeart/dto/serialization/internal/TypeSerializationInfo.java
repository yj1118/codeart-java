package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import apros.codeart.dto.DTObject;
import apros.codeart.dto.serialization.IDTOSerializable;
import apros.codeart.runtime.Activator;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.PrimitiveUtil;
import apros.codeart.util.StringUtil;

public abstract class TypeSerializationInfo {
	private DTOClassImpl _classAnn;

	public DTOClassImpl getClassAnn() {
		return _classAnn;
	}

	private Class<?> _targetClass;

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

		var fields = this.getTargetClass().getDeclaredFields();
		for (var field : fields) {
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

	public DTObject serialize(Object instance) {
		var dto = DTObject.editable();

		var serializable = TypeUtil.as(instance, IDTOSerializable.class);
		if (serializable != null) {
			serializable.serialize(dto, StringUtil.empty()); // string.Empty意味着 序列化的内容会完全替换dto
		} else {
			serialize(instance, dto);
		}
		return dto;
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
		var ctorInfo = _getConstructorInfo.apply(_targetClass);
		if (ctorInfo.hasNoArguments) {
			var instance = Activator.createInstance(targetClass);
			deserialize(instance, dto);
			return instance;
		} else {
			var ctor = ctorInfo.find(dto);
			if (ctor == null)
				throw new NotFoundCtorException(targetClass);

			var prms = ctor.getParameters();
			var types = new Class<?>[1 + prms.length];
			types[0] = targetClass;
			var args = new Object[prms.length];

			for (var i = 0; i < prms.length; i++) {
				var prm = prms[i];
				var type = prm.getType();

				if (PrimitiveUtil.is(type)) {
					var value = dto.getValue(prm.getName());
					args[i] = value;
				} else {
					var obj = dto.getObject(prm.getName());
					var value = obj.save(type);
					args[i] = value;
				}
				types[i + 1] = type;
			}

			var instance = Activator.createInstance(types, args);
			deserialize(instance, dto);
			return instance;
		}

	}

	private static record ConstructorInfo(boolean hasNoArguments, Iterable<Constructor<?>> constructors) {

		public Constructor<?> find(DTObject dto) {
			for (var ctor : this.constructors()) {
				boolean fined = true;
				for (var p : ctor.getParameters()) {
					if (!dto.exist(p.getName())) {
						fined = false;
						break;
					}
				}
				if (fined)
					return ctor;
			}
			return null;
		}

	}

	private static final Function<Class<?>, ConstructorInfo> _getConstructorInfo = LazyIndexer.init((instanceType) -> {
		var ctors = ListUtil.filter(instanceType.getConstructors(), (ctor) -> Modifier.isPublic(ctor.getModifiers()));
		for (var ctor : ctors) {
			if (ctor.getParameterCount() == 0)
				return new ConstructorInfo(true, ctors);
		}
		return new ConstructorInfo(false, ctors);
	});

	/**
	 * 用 dto 里的数据，填充 instance 的属性
	 * 
	 * @param instance
	 * @param dto
	 */
	public abstract void deserialize(Object instance, DTObject dto);

}
