package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.Util.as;
import static com.apros.codeart.runtime.Util.is;

import com.apros.codeart.context.ContextSession;
import com.apros.codeart.dto.serialization.IDTOSerializable;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

public class DTObject implements AutoCloseable {

	private DTEObject _root;

	DTEObject getRoot() {
		return _root;
	}

	DTEntity getParent() {
		return _root.getParent();
	}

	void setParent(DTEntity e) {
		_root.setParent(e);
	}

	private boolean _isReadOnly;

	public boolean isReadOnly() {
		return _isReadOnly;
	}

	private void validateReadOnly() {
		if (_isReadOnly)
			throw new IllegalStateException(strings("DTOReadOnly"));
	}

	private DTObject(DTEObject root, boolean isReadOnly) {
		_root = root;
		_isReadOnly = isReadOnly;
	}

//	#region 值

	public String getString(String findExp) {
		return (String) this.get(findExp);
	}

	public String getString(String findExp, String defalutValue) {
		return (String) this.get(findExp, defalutValue);
	}

	public Byte getByte(String findExp) {
		return (Byte) this.get(findExp);
	}

	public Byte getByte(String findExp, Byte defaultValue) {
		return (Byte) this.get(findExp, defaultValue);
	}

	public Integer getInt(String findExp) {
		return (Integer) this.get(findExp);
	}

	public Integer getInt(String findExp, Integer defaultValue) {
		return (Integer) this.get(findExp, defaultValue);
	}

	public Long getLong(String findExp) {
		return (Long) this.get(findExp);
	}

	public Long getLong(String findExp, Long defaultValue) {
		return (Long) this.get(findExp, defaultValue);
	}

	public Boolean getBool(String findExp) {
		return (Boolean) get(findExp, false, true);
	}

	public Boolean getBool(String findExp, boolean defaultValue) {
		return (Boolean) get(findExp, defaultValue, false);
	}

	public Iterable<Long> getLongs(String findExp) {
		return this.getValues(Long.class, findExp);
	}

	private Object extractValue(DTEntity entity) {
		switch (entity.getType()) {
		case DTEntityType.VALUE: {
			var ev = as(entity, DTEValue.class);
			if (ev != null)
				return ev.getValue();
		}
			break;
		case DTEntityType.OBJECT: {
			var eo = as(entity, DTEObject.class);
			if (eo != null)
				return DTObject.obtain(eo, _isReadOnly);
		}
			break;
		case DTEntityType.LIST: {
			var el = as(entity, DTEList.class);
			if (el != null)
				return el.getObjects();
		}
			break;
		}
		return null;
	}

	public Object get(String findExp, Object defaultValue, boolean throwError) {
		DTEntity entity = find(findExp, throwError);
		Object value = entity == null ? null : extractValue(entity);
		return value == null ? defaultValue : value;
	}

	public Object get(String findExp, Object defaultValue) {
		return get(findExp, defaultValue, false);
	}

	public Object get(String findExp) {
		return get(findExp, null, true);
	}

	public Object get() {
		return get(StringUtil.empty(), true);
	}

	public void set(String findExp, Object value) {
		validateReadOnly();

		var dtoValue = as(value, DTObject.class);
		if (dtoValue != null) {
			setObject(findExp, dtoValue);
			return;
		}

		var es = finds(findExp, false);
		if (Iterables.size(es) == 0) {
			var query = QueryExpression.create(findExp);
			_root.setMember(query, (name) -> {
				return createEntity(name, value);
			});
		} else {
			var isPureValue = isPureValue(value);
			for (var e : es) {
				if (e.getType() == DTEntityType.VALUE && isPureValue) {
					var ev = as(e, DTEValue.class);
					ev.setValue(value);
					continue;
				}

				var parent = as(e.getParent(), DTEObject.class);
				if (parent == null)
					throw new IllegalStateException(strings("DTOExpressionError", findExp));

				var query = QueryExpression.create(e.getName());
				parent.setMember(query, (name) -> {
					return createEntity(name, value);
				});
			}
		}
	}

	public void set(Object value) {
		set(StringUtil.empty(), value);
	}

	private void setObject(String findExp, DTObject obj) {
		validateReadOnly();

		if (StringUtil.isNullOrEmpty(findExp)) {
			// dto.Set(newDTO) 这种表达式下说明此时需要替换整个dto
			// 为了保证数据安全，需要克隆，{xxx:{a,b}},如果不克隆，那么b=xxx就会出现错误
			var newRoot = (DTEObject) obj.getRoot().clone();
			newRoot.setParent(_root.getParent());
			_root = newRoot;
		} else {
			var query = QueryExpression.create(findExp);
			_root.setMember(query, (name) -> {
				var e = (DTEObject) obj.getRoot().clone();
				e.setName(name);
				return e;
			});
		}
	}

	/**
	 * 用 obj 的内容替换当前对象
	 * 
	 * @param obj
	 */
	public void replace(DTObject obj) {
		setObject(StringUtil.empty(), obj);
	}

	private DTEntity createEntity(String name, Object value) {
		var list = as(value, Iterable.class);
		if (list != null)
			return createListEntity(name, list);

		var dto = as(value, DTObject.class);
		if (dto != null) {
			var root = dto.getRoot();
			root.setName(name);
			return root;
		} else {
			return DTEValue.obtainByValue(name, value);
		}
	}

	private DTEList createListEntity(String name, Iterable<?> list) {
		var dte = DTEList.obtain(name);

		for (var item : list) {
			dte.push((dto) -> {
				dto.set(item);
			});
		}
		return dte;
	}

	private DTEntity find(String findExp, boolean throwError) {
		var query = QueryExpression.create(findExp);

		DTEntity entity = null;
		var es = _root.finds(query);
		if (Iterables.size(es) > 0)
			entity = ListUtil.first(es);

		if (entity == null) {
			if (throwError)
				throw new IllegalStateException(strings("DTOEntityNotFound", findExp));
			return null;
		}
		return entity;
	}

	private <T> T find(Class<T> cls, String findExp, boolean throwError) {
		DTEntity e = find(findExp, throwError);
		if (e == null)
			return null;
		T entity = as(e, cls);
		if (entity == null && throwError)
			throw new IllegalStateException(strings("DTOMemberNotMatch", findExp, cls.getName()));
		return entity;
	}

	public boolean exist(String findExp) {
		return find(findExp, false) != null;
	}

	private Iterable<DTEntity> finds(String findExp, boolean throwError) {
		var query = QueryExpression.create(findExp);

		var es = _root.finds(query);

		if (Iterables.size(es) == 0 && throwError) {
			throw new IllegalStateException(strings("DTOEntityNotFound", findExp));
		}
		return es;
	}

	Iterable<DTEntity> getMembers() {
		return _root.getMembers();
	}

	public Iterable<DTObject> getObjects(String findExp, boolean throwError) {
		DTEList entity = find(DTEList.class, findExp, throwError);
		if (entity == null)
			return null;
		return entity.getObjects();
	}

//	private Iterable<Long> getLongs(String findExp, Long itemDefaultValue, boolean throwError) {
//		DTEList entity = find(DTEList.class, findExp, throwError);
//		if (entity == null)
//			return null;
//		return entity.getValues(Long.class, defaultValue);
//	}

	public <T> Iterable<T> getValues(Class<T> itemClass, String findExp) {
		return getValues(itemClass, findExp, null, true);
	}

	private <T> Iterable<T> getValues(Class<T> itemClass, String findExp, T itemDefaultValue, boolean throwError) {
		DTEList entity = find(DTEList.class, findExp, throwError);
		if (entity == null)
			return null;
		return entity.getValues(itemClass, itemDefaultValue, throwError);
	}

	/**
	 * 是否为单值dto，即：{value}的形式
	 * 
	 * @return
	 */
	public boolean isSingleValue() {
		return _root.isSingleValue();
	}

	// internal void OrderEntities()
	// {
//	    _root.OrderEntities();
	// }

	/**
	 * 是否为纯值
	 * 
	 * @param value
	 * @return
	 */
	private static boolean isPureValue(Object value) {
		if (is(value, DTObject.class) || isList(value))
			return false;
		return true;
	}

	private static boolean isList(Object value) {
		return is(value, Iterable.class);
	}

	public DTObject clone() {
		return obtain((DTEObject) _root.clone(), _isReadOnly);
	}

	public String getCode() {
		return getCode(false, false);
	}

	public String getCode(boolean sequential, boolean outputName) {
		return null;
	}

	public String getSchemaCode(boolean sequential, boolean outputName) {
		StringBuilder code = new StringBuilder();
		_root.fillSchemaCode(code, sequential, outputName);
		return code.toString();
	}

	public boolean hasData() {
		return false;
	}

	/**
	 * 无视只读标记，强制清理数据
	 * 
	 * @throws Exception
	 */
	void forceClearData() {
		_root.clearData();
	}

	@Override
	public void close() throws Exception {
		_root = null;
	}

	public void clearData() {
		validateReadOnly();
		_root.clearData();
	}

//	#region 代码

	public String GetCode() {
		return GetCode(false, true);
	}

	public String GetCode(boolean sequential) {
		return GetCode(sequential, true);
	}

	public String GetSchemaCode() {
		return GetSchemaCode(false, true);
	}

	public String GetCode(boolean sequential, boolean outputName) {
		StringBuilder code = new StringBuilder();
		fillCode(code, sequential, outputName);
		return code.toString();
	}

	public String GetSchemaCode(boolean sequential, boolean outputName) {
		StringBuilder code = new StringBuilder();
		fillSchemaCode(code, sequential, outputName);
		return code.toString();
	}

	void fillCode(StringBuilder code, boolean sequential, boolean outputName) {
		_root.fillCode(code, sequential, outputName);
	}

	void fillSchemaCode(StringBuilder code, boolean sequential, boolean outputName) {
		_root.fillSchemaCode(code, sequential, outputName);
	}

//	#endregion

	private static DTObject createImpl(String code, boolean isReadOnly) {
		var root = EntityDeserializer.deserialize(code, isReadOnly);
		return obtain(root, isReadOnly);
	}

	/**
	 * 创建非只读的dto对象
	 * 
	 * @param code
	 * @return
	 */
	public static DTObject Create(String code) {
		if (StringUtil.isNullOrEmpty(code))
			return DTObject.Create();
		return createImpl(code, false);
	}

	public static DTObject Create() {
		return createImpl("{}", false);
	}

	/**
	 * 根据架构代码将对象的信息加载到dto中
	 * 
	 * @param schemaCode
	 * @param target
	 * @return
	 */
	public static DTObject create(String schemaCode, Object target) {
		var dy = as(target, IDTOSerializable.class);
		if (dy != null) {
			return create(schemaCode, dy.getData());
		}

		var dto = as(target, DTObject.class);
		if (dto != null) {
			DTObject result = DTObject.Create();
			result.load(schemaCode, dto);
			return result;
		}
		return DTObjectMapper.Instance.Load(schemaCode, target);
	}

//	#region 对象映射

	/// <summary>
	/// 根据架构代码，将dto的数据创建到新实例<paramref name="instanceType"/>中
	/// </summary>
	/// <param name="instanceType"></param>
	/// <param name="schemaCode"></param>
	/// <param name="dto"></param>
	/// <returns></returns>
	public object Save(Type instanceType, string schemaCode) {
		return DTObjectMapper.Instance.Save(instanceType, schemaCode, this);
	}

	/// <summary>
	/// 根据架构代码，将dto的数据创建到新实例<paramref name="instanceType"/>中
	/// </summary>
	/// <param name="instanceType"></param>
	/// <returns></returns>
	public object Save(Type instanceType) {
		return Save(instanceType, string.Empty);
	}

	/// <summary>
	/// 根据架构代码，将dto中的数据全部保存到类型为<typeparamref name="T"/>的实例中
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="schemaCode"></param>
	/// <returns></returns>
	public void Save<T>(
	T obj, string schemaCode)
	{
		var instanceType = typeof(T);
		DTObjectMapper.Instance.Save(obj, schemaCode, this);
	}

	/// <summary>
	/// 将dto中的数据全部保存到类型为<typeparamref name="T"/>的实例中
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <returns></returns>
	public void Save<T>(
	T obj)
	{
	     Save<T>(obj, string.Empty);
	 }

	/// <summary>
	/// 根据架构代码，将dto中的数据全部保存到类型为<typeparamref name="T"/>的实例中
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="schemaCode"></param>
	/// <returns></returns>
	public T Save<T>(
	string schemaCode)
	{
		var instanceType = typeof(T);
		return (T) Save(instanceType, schemaCode);
	}

	/// <summary>
	/// 将dto中的数据全部保存到类型为<typeparamref name="T"/>的实例中
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <returns></returns>
	public T Save<T>()
	{
		return Save < T > (string.Empty);
	}

	/// <summary>
	/// 根据架构代码将对象的信息加载到dto中
	/// </summary>
	/// <param name="schemaCode"></param>
	/// <param name="target"></param>
	/// <returns></returns>
	public void Load(string schemaCode, object target)
	 {
	     var dy = target as IDTOSerializable;
	     if(dy != null)
	     {
	         Load(schemaCode,dy.GetData());
	         return;
	     }

	     var dto = target as DTObject;
	     if (dto != null)
	     {
	         Load(schemaCode, target);
	         return;
	     }
	     DTObjectMapper.Instance.Load(this, schemaCode, target);
	 }

	private void load(String schemaCode, DTObject target) {
		var schema = DTObject.Create(schemaCode);
		var entities = schema.getMembers();
		for (var entity : entities) {
			var name = entity.getClass();
			if (target.exist(name)) {
				this.setValue(name, target.getValue(name));
			}
		}
	}

	/// <summary>
	/// 将<paramref name="target"/>里面的所有属性的值加载到dto中
	/// </summary>
	/// <param name="target"></param>
	public void load(Object target) {
		load(StringUtil.empty(), target);
	}

//	#endregion

	static DTObject obtain(DTEObject root, boolean isReadOnly) {
		return ContextSession.registerItem(new DTObject(root, isReadOnly));
	}

	static DTObject obtain() {
		return ContextSession.registerItem(new DTObject(DTEObject.obtain(StringUtil.empty()), false));
	}

}
