package com.apros.codeart.dto.serialization;

import java.lang.reflect.Field;
import java.util.Map;

import com.apros.codeart.bytecode.MethodGenerator;
import com.apros.codeart.runtime.FieldUtil;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.StringUtil;

class MemberSerializationInfo {
//	#region 静态构造

	private static Class<?> getTargetClass(Class<?> classType, Field field) {
		if (classType != null)
			return classType;
		return field.getType();
	}

	private static MemberSerializationInfo createByCollection(Class<?> targetClass, Field field,
			DTOMemberAnnotation memberAnn) {
		if (TypeUtil.is(targetClass, Iterable.class)) {
			return field == null ? new CollectionSerializationInfo(targetClass)
					: new CollectionSerializationInfo(field, memberAnn);
		}

		if (TypeUtil.is(targetClass, Map.class)) {
			throw new IllegalStateException("暂时不支持键值对的dto序列化操作"); // todo
		}

		return null;

	}

	public static MemberSerializationInfo create(Field field, DTOMemberAnnotation memberAnn) {
		var t = getTargetClass(null, field);
		// 数组
		if (t.isArray())
			return new ArraySerializationInfo(field, memberAnn);
		// ICollection或IDictionary
		MemberSerializationInfo info = createByCollection(t, field, memberAnn);
		if (info != null)
			return info;
		// 普通类型
		return new MemberSerializationInfo(field, memberAnn);
	}

	public static MemberSerializationInfo create(Class<?> classType) {
		// 数组
		if (classType.isArray())
			return new ArraySerializationInfo(classType);
		// ICollection或IDictionary
		MemberSerializationInfo info = createByCollection(classType, null, null);
		if (info != null)
			return info;
		// 普通类型
		return new MemberSerializationInfo(classType);
	}

//	#endregion

	private Field _field;

	public Field getField() {
		return _field;
	}

	private Class<?> _classType;

	private DTOMemberAnnotation _memberAnn;

	public DTOMemberAnnotation getMemberAnn() {
		return _memberAnn;
	}

	/**
	 * 字段名称
	 * 
	 * @return
	 */
	public String getName() {
		return _field.getName();
	}

	public Class<?> getOwnerClass() {
		if (_classType != null)
			return _classType;
		return _field.getDeclaringClass(); // 申明该字段的类
	}

	public boolean isAbstract() {
		return TypeUtil.isAbstract(_field.getType());
	}

//	#region 序列化的目标

	public Class<?> getTargetClass() {
		return getTargetClass(_classType, _field);
	}

	public boolean isClassInfo() {
		return _classType != null;
	}

//	#endregion

	public MemberSerializationInfo(Class<?> classType) {
		_classType = classType;
	}

	public MemberSerializationInfo(Field field, DTOMemberAnnotation memberAnn) {
		_field = field;
		_memberAnn = memberAnn;
	}

	/**
	 * 生成序列化代码
	 * 
	 * @param g
	 */
	public void generateSerializeIL(MethodGenerator g) {
		// serializer.serialize(v); 或 //writer.Writer(v);
		SerializationMethodHelper.write(g, this.getDTOMemberName(), this.getTargetClass(), (argType) -> {
			loadMemberValue(g);
		});
	}

	/**
	 * 加载成员的值到堆栈上
	 * 
	 * @param g
	 */
	protected void loadMemberValue(MethodGenerator g) {

		if (this.isClassInfo()) {
			loadOwner(g);
			return;
		}

		g.loadField(SerializationArgs.InstanceName, _field.getName());
	}

	/// <summary>
	/// 生成反序列化代码
	/// </summary>
	/// <param name="g"></param>
	public void generateDeserializeIL(MethodGenerator g) {
		setMember(g, () -> {
			SerializationMethodHelper.read(g, this.getDTOMemberName(), this.getTargetClass());
		});
	}

	public void setMember(MethodGenerator g, Runnable loadValue) {
		if (this.isClassInfo()) {
			g.assign(SerializationArgs.InstanceName, loadValue);
		} else {

			g.assignField(() -> {
				loadOwner(g);
			}, this.getField().getName(), loadValue);
		}
	}

	private void loadOwner(MethodGenerator g) {
		g.loadVariable(SerializationArgs.InstanceName);
	}

	public String getDTOMemberName() {
		var memberName = _getDTOMemberName();
		return FieldUtil.getAgreeName(memberName);
	}

	private String _getDTOMemberName() {
		if (this.getMemberAnn() != null && !StringUtil.isNullOrEmpty(this.getMemberAnn().getName()))
			return this.getMemberAnn().getName();
		if (!StringUtil.isNullOrEmpty(_field.getName()))
			return StringUtil.firstToUpper(_field.getName());
		return StringUtil.empty();
	}

	public String getDTOSchemaCode() {
		return this.getDTOMemberName();
	}
}
