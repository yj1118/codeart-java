package com.apros.codeart.dto.serialization;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.apros.codeart.dto.DTObject;
import com.apros.codeart.runtime.Activator;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.StringUtil;
import com.google.common.base.Preconditions;

abstract class TypeSerializationInfo {
	private DTOClassAnn _classAnn;

	public DTOClassAnn getClassAnn() {
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

	protected abstract DTOClassAnn getClassAnnotation(Class<?> classType);

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

	protected abstract DTOMemberAnn getMemberAnnotation(Field field);

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
		var instance = Activator.createInstance(this.getTargetClass());
		deserialize(instance, dto);
		return instance;
	}

	/**
	 * 用 dto 里的数据，填充 instance 的属性
	 * 
	 * @param instance
	 * @param dto
	 */
	public abstract void deserialize(Object instance, DTObject dto);

}
