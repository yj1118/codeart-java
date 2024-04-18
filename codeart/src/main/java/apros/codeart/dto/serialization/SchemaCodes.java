package apros.codeart.dto.serialization;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import apros.codeart.dto.DTEntity;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.FieldUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.StringUtil;

class SchemaCodes {

	/**
	 * 成员名称 -> 架构代码
	 */
	private HashMap<String, String> _codes;

	/// <summary>
	/// 类型->架构代码
	/// </summary>
	private HashMap<Class<?>, String> _typeCodes;

	private DTObject _schema;

	private boolean _containsAll;

	public boolean containsAll() {
		return _containsAll;
	}

	public SchemaCodes(Class<?> classType, String schemaCode) {
		_codes = new HashMap<String, String>();
		_typeCodes = new HashMap<Class<?>, String>();

		_containsAll = StringUtil.isNullOrEmpty(schemaCode);
		_schema = DTObject.readonly(schemaCode);
		initialize(classType, schemaCode);
	}

	private boolean tryAddTypeCode(Class<?> classType, String schemaCode) {
		if (!_typeCodes.containsKey(classType)) {
			_typeCodes.put(classType, schemaCode); // 为当前对象类型建立 类型->架构代码的映射
			return true;
		}
		return false;
	}

	private void initialize(Class<?> classType, String schemaCode) {
		tryAddTypeCode(classType, schemaCode); // 为当前对象类型建立 类型->架构代码的映射

		var fields = classType.getDeclaredFields();
		for (var field : fields) {
			var name = FieldUtil.getAgreeName(field.getName());
			var entity = _schema.find(name, false);
			if (entity != null) {
				var memberType = field.getType();
				collectSchemaCode(entity, memberType);
			}
		}
	}

	private void collectSchemaCode(DTEntity entity, Class<?> objectType) {
		if (objectType.isPrimitive()) {
			_codes.put(entity.getName(), entity.getSchemaCode(false));
			return;
		}

		// 以下是处理集合和对象的类型
		if (TypeUtil.isCollection(objectType)) {
			var elementType = TypeUtil.resolveElementType(objectType);
			var sc = entity.getSchemaCode(false);
			var pos = sc.indexOf(":");
			if (pos > -1) {
				// 对于集合类型找出成员的架构代码
				var elementSC = StringUtil.substr(sc, pos + 1); // 定义了架构代码
				elementSC = StringUtil.trimStart(sc, "[");
				elementSC = StringUtil.trimEnd(sc, "]");
				_codes.put(entity.getName(), elementSC);
				// 记忆一次
				tryAddTypeCode(elementType, elementSC);
				return;
			}

			var code = _typeCodes.get(elementType);
			// 没有定义架构代码，先尝试从类型代码中获取
			if (code != null) {
				_codes.put(entity.getName(), code);
			} else {
				_codes.put(entity.getName(), StringUtil.empty()); // 定义空的架构，代表全部属性
			}
		} else {
			// Object的处理
			var sc = entity.getSchemaCode(false);
			var pos = sc.indexOf(":");
			if (pos > -1) {
				sc = StringUtil.substr(sc, pos + 1); // 定义了架构代码
				_codes.put(entity.getName(), sc);
				// 记忆一次
				tryAddTypeCode(objectType, sc);
				return;
			}

			var code = _typeCodes.get(objectType);
			// 没有定义架构代码，先尝试从类型代码中获取
			if (code != null) {
				_codes.put(entity.getName(), code);
			} else {
				_codes.put(entity.getName(), StringUtil.empty()); // 定义空的架构，代表全部属性
			}
		}
	}

	public boolean canMarkup(String memberName) {
		if (this._containsAll)
			return true;
		return _schema.find(memberName, false) != null;
	}

	public String getSchemaCode(String memberName, Supplier<Class<?>> getObjectType) {
		if (this._containsAll)
			return StringUtil.empty(); // 空字符串代表取所有成员
		String code = _codes.get(memberName);
		if (code != null)
			return code;
		String typeCode = _typeCodes.get(getObjectType.get());
		if (typeCode != null)
			return typeCode;
		return null;
	}

	public void sort(List<MemberSerializationInfo> members) {
		members.sort((x, y) -> compare(x, y));
	}

	private int getMemberIndex(MemberSerializationInfo member) {
		int index = 0;
		var es = _schema.getRoot().getMembers();
		for (var e : es) {
			if (e.getName().equalsIgnoreCase(member.getName()))
				return index;
			index++;
		}
		return index;
	}

	public int compare(MemberSerializationInfo x, MemberSerializationInfo y) {
		return Integer.compare(getMemberIndex(x), getMemberIndex(y));
	}
}
