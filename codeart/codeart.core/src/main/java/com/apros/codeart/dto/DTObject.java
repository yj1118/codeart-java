package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.TypeUtil.as;
import static com.apros.codeart.runtime.TypeUtil.is;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.apros.codeart.context.ContextSession;
import com.apros.codeart.dto.serialization.DTObjectMapper;
import com.apros.codeart.dto.serialization.IDTOSerializable;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

/**
 * 本次升级，重写了底层算法，特点：
 * 
 * 1.在会话上下文结束后，会切断双向引用，避免内存泄露
 * 
 * 2.解析字符串后，各项成员不实际存放值，而是存放的值对应的字符串，当获取的时候，可以显示调用getInt等基元类型的操作，返回基元值，避免装箱。
 * 
 * 3.只读模式的dto底层用ArrayList存放数据，可编辑的dto用LinkedList存放数据
 * 
 * 我们基于这样的事实设计DTO：接受字符串，使用一次给调用方，或者将数据给与DTO，DOT生成JSON格式字符串，发送到网络。
 * 
 * 在这种模式下，避免了装箱和拆箱，性能良好。
 * 
 * 但是要频繁的操作dto的同样的值，比如getInt("value")或者setInt("value")调用好几遍，那么建议用getIntRef这种引用系方法。
 */
public class DTObject implements AutoCloseable {

	private DTEObject _root;

	public DTEObject getRoot() {
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

	DTObject(DTEObject root, boolean isReadOnly) {
		_root = root;
		_isReadOnly = isReadOnly;
	}

//	#region 值

	public Byte getByteRef(String findExp) {
		return (Byte) this.getValue(findExp);
	}

	public Byte getByteRef(String findExp, Byte defaultValue) {
		return (Byte) this.getValue(findExp, defaultValue);
	}

	public Short getShortRef(String findExp, Short defaultValue) {
		return (Short) this.getValue(findExp, defaultValue);
	}

	public Integer getIntRef(String findExp) {
		return (Integer) this.getValue(findExp);
	}

	public Integer getIntRef(String findExp, Integer defaultValue) {
		return (Integer) this.getValue(findExp, defaultValue);
	}

	public Long getLongRef(String findExp) {
		return (Long) this.getValue(findExp);
	}

	public Long getLongRef(String findExp, Long defaultValue) {
		return (Long) this.getValue(findExp, defaultValue);
	}

	public Boolean getBooleanRef(String findExp) {
		return (Boolean) getValue(findExp, false, true);
	}

	public Boolean getBooleanRef(String findExp, boolean defaultValue) {
		return (Boolean) getValue(findExp, defaultValue, false);
	}

//	public Iterable<Long> getLongRefs(String findExp) {
//		return this.getValues(Long.class, findExp);
//	}

	private Object extractValueRef(DTEntity entity) {
		switch (entity.getType()) {
		case DTEntityType.VALUE: {
			var ev = as(entity, DTEValue.class);
			if (ev != null)
				return ev.getValueRef();
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

	public Object getValue() {
		return getValue(StringUtil.empty(), true);
	}

	public Object getValue(String findExp) {
		return getValue(findExp, null, true);
	}

	public Object getValue(String findExp, Object defaultValue) {
		return getValue(findExp, defaultValue, false);
	}

	Object getValue(String findExp, Object defaultValue, boolean throwError) {
		DTEntity entity = find(findExp, throwError);
		Object value = entity == null ? null : extractValueRef(entity);
		return value == null ? defaultValue : value;
	}

	public DTObject getObject(String findExp) {
		return getObject(findExp, null, true);
	}

	public DTObject getObject(String findExp, DTObject defaultValue) {
		return getObject(findExp, defaultValue, false);
	}

	private DTObject getObject(String findExp, DTObject defaultValue, boolean throwError) {
		var entity = this.find(DTEObject.class, findExp, throwError);
		return entity == null ? defaultValue : DTObject.obtain(entity, true);
	}

	// region 不必装箱和拆箱的操作

	public boolean getBoolean(String findExp, boolean defaultValue) {
		return getBoolean(findExp, defaultValue, false);
	}

	public boolean getBoolean(String findExp) {
		return getBoolean(findExp, false, true);
	}

	private boolean getBoolean(String findExp, boolean defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getBoolean();
	}

	public byte getByte(String findExp, byte defaultValue) {
		return getByte(findExp, defaultValue, false);
	}

	public byte getByte(String findExp) {
		return getByte(findExp, (byte) 0, true);
	}

	private byte getByte(String findExp, byte defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getByte();
	}

	public char getChar(String findExp, char defaultValue) {
		return getChar(findExp, defaultValue, false);
	}

	public char getChar(String findExp) {
		return getChar(findExp, StringUtil.charEmpty(), true);
	}

	private char getChar(String findExp, char defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getChar();
	}

	public short getShort(String findExp, short defaultValue) {
		return getShort(findExp, defaultValue, false);
	}

	public short getShort(String findExp) {
		return getShort(findExp, (short) 0, true);
	}

	private short getShort(String findExp, short defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getShort();
	}

	public int getInt(String findExp, int defaultValue) {
		return getInt(findExp, defaultValue, false);
	}

	public int getInt(String findExp) {
		return getInt(findExp, 0, true);
	}

	private int getInt(String findExp, int defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getInt();
	}

	public long getLong(String findExp, long defaultValue) {
		return getLong(findExp, defaultValue, false);
	}

	public long getLong(String findExp) {
		return getLong(findExp, 0L, true);
	}

	public long getLong(String findExp, long defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getLong();
	}

	public float getFloat(String findExp, float defaultValue) {
		return getFloat(findExp, defaultValue, false);
	}

	public float getFloat(String findExp) {
		return getFloat(findExp, 0F, true);
	}

	public float getFloat(String findExp, float defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getFloat();
	}

	public double getDouble(String findExp, double defaultValue) {
		return getDouble(findExp, defaultValue, false);
	}

	public double getDouble(String findExp) {
		return getDouble(findExp, (double) 0, true);
	}

	public double getDouble(String findExp, double defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getDouble();
	}

	public String getString() {
		return getString(null, null, true);
	}

	public String getString(String findExp, String defaultValue) {
		return getString(findExp, defaultValue, false);
	}

	public String getString(String findExp) {
		return getString(findExp, null, true);
	}

	public String getString(String findExp, String defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getString();
	}

	public LocalDateTime getLocalDateTime(String findExp, LocalDateTime defaultValue) {
		return getLocalDateTime(findExp, defaultValue, false);
	}

	public LocalDateTime getLocalDateTime(String findExp) {
		return getLocalDateTime(findExp, LocalDateTime.MIN, true);
	}

	public LocalDateTime getLocalDateTime(String findExp, LocalDateTime defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getLocalDateTime();
	}

	public Instant getInstant(String findExp, Instant defaultValue) {
		return getInstant(findExp, defaultValue, false);
	}

	public Instant getInstant(String findExp) {
		return getInstant(findExp, Instant.MIN, true);
	}

	public Instant getInstant(String findExp, Instant defaultValue, boolean throwError) {
		DTEValue entity = find(DTEValue.class, findExp, throwError);
		return entity == null ? defaultValue : entity.getInstant();
	}

	public void setByte(String findExp, byte value) {
		setValue(findExp, Byte.toString(value), false);
	}

	public void setShort(String findExp, short value) {
		setValue(findExp, Short.toString(value), false);
	}

	public void setInt(String findExp, int value) {
		setValue(findExp, Integer.toString(value), false);
	}

	public void setLong(String findExp, long value) {
		setValue(findExp, Long.toString(value), false);
	}

	public void setFloat(String findExp, float value) {
		setValue(findExp, Float.toString(value), false);
	}

	public void setDouble(String findExp, double value) {
		setValue(findExp, Double.toString(value), false);
	}

	public void setBoolean(String findExp, boolean value) {
		setValue(findExp, Boolean.toString(value), false);
	}

	public void setChar(String findExp, char value) {
		setValue(findExp, Character.toString(value), false);
	}

	public void setString(String findExp, String value) {
		setValue(findExp, value, true);
	}

	private void setValue(String findExp, String valueCode, boolean valueIsString) {
		validateReadOnly();

		var es = finds(findExp, false);
		if (Iterables.size(es) == 0) {
			var query = QueryExpression.create(findExp);
			_root.setMember(query, (name) -> {
				return createPrimitiveEntity(name, valueCode);
			});
		} else {
			for (var e : es) {
				if (e.getType() == DTEntityType.VALUE) {
					var ev = as(e, DTEValue.class);
					ev.setValueCode(valueCode, valueIsString); // 基元值，不是字符串
					continue;
				}

				var parent = as(e.getParent(), DTEObject.class);
				if (parent == null)
					throw new IllegalStateException(strings("DTOExpressionError", findExp));

				var query = QueryExpression.create(e.getName());
				parent.setMember(query, (name) -> {
					return createPrimitiveEntity(name, valueCode);
				});
			}
		}
	}

	// endregion

	public void setLocalDateTime(String findExp, LocalDateTime value) {
		setValueRef(findExp, value, true); // 时间转换成json，会带""号，所以valueCodeIsString是true
	}

	public void setInstant(String findExp, Instant value) {
		setValueRef(findExp, value, true);
	}

	public void setValue(String findExp, Object value) {
		setValueRef(findExp, value, Util.getValueCodeIsString(value));
	}

	public void setValue(Object value) {
		setValueRef(StringUtil.empty(), value, Util.getValueCodeIsString(value));
	}

	private void setValueRef(String findExp, Object value, boolean valueCodeIsString) {
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
				return createEntity(name, value, valueCodeIsString);
			});
		} else {
			var isPureValue = isPureValue(value);
			for (var e : es) {
				if (e.getType() == DTEntityType.VALUE && isPureValue) {
					var ev = as(e, DTEValue.class);
					ev.setValueRef(value, valueCodeIsString);
					continue;
				}

				var parent = as(e.getParent(), DTEObject.class);
				if (parent == null)
					throw new IllegalStateException(strings("DTOExpressionError", findExp));

				var query = QueryExpression.create(e.getName());
				parent.setMember(query, (name) -> {
					return createEntity(name, value, valueCodeIsString);
				});
			}
		}
	}

	private void setValueRef(Object value, boolean valueCodeIsString) {
		setValueRef(StringUtil.empty(), value, valueCodeIsString);
	}

	public void setObject(String findExp, DTObject obj) {
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

	private DTEntity createEntity(String name, Object value, boolean valueCodeIsString) {
		var list = as(value, Iterable.class);
		if (list != null)
			return createListEntity(name, list);

		var dto = as(value, DTObject.class);
		if (dto != null) {
			var root = dto.getRoot();
			root.setName(name);
			return root;
		} else {
			return DTEValue.obtainByValue(name, value, valueCodeIsString);
		}
	}

	private DTEList createListEntity(String name, Iterable<?> list) {
		var dte = DTEList.obtainEditable(name);

		for (var item : list) {
			dte.push((dto) -> {
				dto.setValueRef(item, true);
			});
		}
		return dte;
	}

	private DTEntity createPrimitiveEntity(String name, String valueCode) {
		return DTEValue.obtainByCode(name, valueCode, false);
	}

	public boolean exist(String findExp) {
		return find(findExp, false) != null;
	}

	public DTEntity find(String findExp, boolean throwError) {
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

	Iterable<DTEntity> finds(String findExp, boolean throwError) {
		var query = QueryExpression.create(findExp);

		var es = _root.finds(query);

		if (Iterables.size(es) == 0 && throwError) {
			throw new IllegalStateException(strings("DTOEntityNotFound", findExp));
		}
		return es;
	}

	public Iterable<DTEntity> getMembers() {
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

//	public <T> Iterable<T> getValues(Class<T> itemClass, String findExp) {
//		return getValues(itemClass, findExp, null, true);
//	}
//
//	private <T> Iterable<T> getValues(Class<T> itemClass, String findExp, T itemDefaultValue, boolean throwError) {
//		DTEList entity = find(DTEList.class, findExp, throwError);
//		if (entity == null)
//			return null;
//		return entity.getValues(itemClass, itemDefaultValue, throwError);
//	}

	/**
	 * 如果不存在findExp对应的列表，那么创建
	 * 
	 * @param findExp
	 */
	public void obtainList(String findExp) {
		validateReadOnly();

		DTEList entity = find(DTEList.class, findExp, false);
		if (entity == null) {
			var query = QueryExpression.create(findExp);
			_root.setMember(query, (name) -> {
				return DTEList.obtainEditable(name);
			});
		}
	}

	private void setList(String findExp, Iterable<DTObject> items) {
		validateReadOnly();

		push(findExp, null);// 以此来防止当items个数为0时，没有创建的bug
		if (items == null)
			return;
		for (var item : items) {
			push(findExp, item);
		}
	}

	public List<DTObject> getList(String findExp) {
		return getList(findExp, true);
	}

	public List<DTObject> getList(String findExp, boolean throwError) {
		DTEList entity = find(DTEList.class, findExp, throwError);
		if (entity == null)
			return null;
		return entity.getObjects();
	}

	/// <summary>
	/// 向集合追加一个成员
	/// </summary>
	/// <param name="findExp"></param>
	/// <param name="member"></param>
	private void push(String findExp, DTObject member) {
		validateReadOnly();

		DTEList entity = getOrCreateList(findExp);
		if (member == null)
			return;

		entity.push(member);
	}

	/**
	 * 填充dto成员，然后追加到集合，不用重复查找，比较高效
	 * 
	 * @param <T>
	 * @param findExp
	 * @param list
	 * @param action
	 */
	public <T> void push(String findExp, Iterable<T> list, BiConsumer<DTObject, T> action) {

		validateReadOnly();

		DTEList entity = getOrCreateList(findExp);
		for (T item : list) {
			DTObject dto = entity.push();
			action.accept(dto, item);
		}
	}

	public <T> void push(Iterable<T> list, BiConsumer<DTObject, T> action) {
		this.<T>push(StringUtil.empty(), list, action);
	}

	public <T> void push(String findExp, Iterable<T> list, Function<T, DTObject> factory) {
		validateReadOnly();

		var entity = getOrCreateList(findExp);

		for (T item : list) {
			DTObject dto = factory.apply(item);
			entity.push(dto);
		}
	}

	public <T> void push(Iterable<T> list, Function<T, DTObject> factory) {
		this.<T>push(StringUtil.empty(), list, factory);
	}

	public DTObject push(String findExp) {
		validateReadOnly();

		DTEList entity = getOrCreateList(findExp);
		return entity.push();
	}

	public DTObject push() {
		return this.push(StringUtil.empty());
	}

	private DTEList getOrCreateList(String findExp) {
		var entity = find(DTEList.class, findExp, false);
		if (entity == null) {
			var query = QueryExpression.create(findExp);
			_root.setMember(query, (name) -> {
				return DTEList.obtainEditable(name);
			});
			entity = find(DTEList.class, findExp, true);
		}
		return entity;
	}

	public DTObject insert(String findExp, int index) {
		validateReadOnly();

		DTEList entity = getOrCreateList(findExp);
		return entity.push(index);
	}

	public void each(String findExp, Consumer<DTObject> action) {
		var list = getList(findExp, false);
		if (list == null)
			return;
		for (var dto : list) {
			action.accept(dto);
		}
	}

	public DTObject top(String findExp, int count) {
		ArrayList<DTObject> data = new ArrayList<DTObject>(count);

		var list = getList(findExp, false);

		if (list != null) {
			for (var dto : list) {
				if (data.size() == count)
					break;
				data.add(dto);
			}
		}

		DTObject result = DTObject.editable();
		result.setList("rows", data);
		return result;
	}

	public DTObject page(String findExp, int pageIndex, int pageSize) {
		var list = getList(findExp, false);
		int dataCount = list == null ? 0 : Iterables.size(list);

		var result = DTObject.editable();
		result.setInt("pageIndex", pageIndex);
		result.setInt("pageSize", pageSize);
		result.setInt("dataCount", dataCount);

		var start = (pageIndex - 1) * pageSize;
		if (start >= dataCount) {
			result.obtainList(findExp);
			return result;
		}

		var end = start + pageSize - 1;
		if (end >= dataCount)
			end = dataCount - 1;

		ArrayList<DTObject> items = new ArrayList<DTObject>(end - start + 1);

		for (var i = start; i <= end; i++) {
			items.add(Iterables.get(list, i));
		}

		result.setList(findExp, items);
		return result;
	}

	public void each(String findExp, Function<DTObject, Boolean> action) {
		var list = getList(findExp, false);
		if (list == null)
			return;
		for (var dto : list) {
			if (!action.apply(dto))
				return; // 如果返回false，表示中断遍历操作
		}
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

	public boolean hasData() {
		return _root.hasData();
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

	/**
	 * 以不干涉排序，输出名称的规则得到json码
	 * 
	 * @return
	 */
	public String getCode() {
		return getCode(false, true);
	}

	/**
	 * 
	 * @param sequential 是否排序
	 * @return
	 */
	public String getCode(boolean sequential) {
		return getCode(sequential, true);
	}

	public String getSchemaCode() {
		return getSchemaCode(false);
	}

	public String getCode(boolean sequential, boolean outputName) {
		StringBuilder code = new StringBuilder();
		fillCode(code, sequential, outputName);
		return code.toString();
	}

	public String getSchemaCode(boolean sequential) {
		StringBuilder code = new StringBuilder();
		fillSchemaCode(code, sequential);
		return code.toString();
	}

	void fillCode(StringBuilder code, boolean sequential, boolean outputName) {
		_root.fillCode(code, sequential, outputName);
	}

	void fillSchemaCode(StringBuilder code, boolean sequential) {
		_root.fillSchemaCode(code, sequential);
	}

//	#endregion

	private static DTObject createImpl(String code, boolean readonly) {
		var root = EntityDeserializer.deserialize(code, readonly);
		return obtain(root, readonly);
	}

	/**
	 * 创建非只读的dto对象
	 * 
	 * @param code
	 * @return
	 */
	public static DTObject readonly(String code) {
		if (StringUtil.isNullOrEmpty(code))
			return DTObject.readonly();
		return createImpl(code, true);
	}

	public static DTObject readonly() {
		return createImpl("{}", true);
	}

	public static DTObject editable(String code) {
		if (StringUtil.isNullOrEmpty(code))
			return DTObject.editable();
		return createImpl(code, false);
	}

	public static DTObject editable() {
		return createImpl("{}", false);
	}

	/**
	 * 根据架构代码将对象的信息加载到dto中
	 * 
	 * @param schemaCode
	 * @param target
	 * @return
	 */
	public static DTObject readonly(String schemaCode, Object target) {
		return createImpl(schemaCode, target, true);
	}

	public static DTObject readonly(Object target) {
		return createImpl(StringUtil.empty(), target, true);
	}

	public static DTObject editable(String schemaCode, Object target) {
		return createImpl(schemaCode, target, false);
	}

	public static DTObject editable(Object target) {
		return createImpl(StringUtil.empty(), target, true);
	}

	/**
	 * 根据架构代码将对象的信息加载到dto中
	 * 
	 * @param schemaCode
	 * @param target
	 * @return
	 */
	public static DTObject createImpl(String schemaCode, Object target, boolean isReadonly) {
		var dy = as(target, IDTOSerializable.class);
		if (dy != null)
			target = dy.getData();

		var dto = as(target, DTObject.class);
		if (dto != null) {
			DTObject result = isReadonly ? DTObject.readonly() : DTObject.editable();
			result.load(schemaCode, dto);
			return result;
		}
		return DTObjectMapper.load(schemaCode, target);
	}

	/**
	 * 根据架构代码，将dto的数据创建到新实例 instanceType 中
	 * 
	 * @param instanceType
	 * @param schemaCode
	 * @return
	 */
	public Object save(Class<?> instanceType, String schemaCode) {
		return DTObjectMapper.save(instanceType, schemaCode, this);
	}

	/**
	 * 根据架构代码，将dto的数据创建到新实例 instanceType 中
	 * 
	 * @param instanceType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T save(Class<T> instanceType) {
		return (T) save(instanceType, StringUtil.empty());
	}

	/**
	 * 根据架构代码，将dto中的数据全部保存到 obj 实例中
	 * 
	 * @param obj
	 * @param schemaCode
	 */
	public void save(Object obj, String schemaCode) {
		DTObjectMapper.save(obj, schemaCode, this);
	}

	/**
	 * 将dto中的数据全部保存到 obj 实例中
	 * 
	 * @param obj
	 */
	public void save(Object obj) {
		save(obj, StringUtil.empty());
	}

//
//	/// <summary>
//	/// 将dto中的数据全部保存到类型为<typeparamref name="T"/>的实例中
//	/// </summary>
//	/// <typeparam name="T"></typeparam>
//	/// <returns></returns>
//	public T Save<T>()
//	{
//		return Save < T > (string.Empty);
//	}
//

	/**
	 * 根据架构代码将对象的信息加载到dto中
	 * 
	 * @param schemaCode
	 * @param target
	 */
	public void load(String schemaCode, Object target) {
		var dy = TypeUtil.as(target, IDTOSerializable.class);
		if (dy != null) {
			load(schemaCode, dy.getData());
			return;
		}

		var dto = TypeUtil.as(target, DTObject.class);
		if (dto != null) {
			load(schemaCode, dto);
			return;
		}
		DTObjectMapper.load(this, schemaCode, target);
	}

	private void load(String schemaCode, DTObject target) {

		Iterable<DTEntity> entities = null;

		if (StringUtil.isNullOrEmpty(schemaCode))
			entities = target.getMembers();
		else {
			var schema = DTObject.readonly(schemaCode); // 这里只是临时用用entities，所以readonly即可
			entities = schema.getMembers();
		}

		for (var entity : entities) {
			var name = entity.getName();
			var te = target.find(name, false);
			if (te != null) {
				var se = this.find(name, false);
				if (se != null)
					se.load(te);
				else {
					this._root.addMember((DTEntity) te.clone());
				}
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

	public static TypeMetadata getMetadata(String metadataCode) {
		return new TypeMetadata(metadataCode);
	}

//	#region 转换

	/// <summary>
	/// 批量变换dto结构，语法：
	/// <para>name=>newName 转换成员名称</para>
	/// <para>value=newValue 赋值</para>
	/// <para>!member 移除表达式对应的成员</para>
	/// <para>~member 保留表达式对应的成员，其余的均移除</para>
	/// 多个表达式可以用;号连接
	/// </summary>
	/// <param name="express">
	/// findExp=>name;findExp=>name
	/// </param>
	public void transform(String express) {
		var expresses = TransformExpressions.create(express);
		for (var exp : expresses) {
			exp.execute(this);
		}
	}

	/// <summary>
	/// 该方法主要用于更改成员值
	/// </summary>
	/// <param name="express">
	/// findExp=valueFindExp
	/// 说明：
	/// valueFindExp 可以包含检索方式，默认的方式是在findExp检索出来的结果中所在的DTO对象中进行检索
	/// 带“@”前缀，表示从根级开始检索
	/// 带“*”前缀，表示返回值所在的对象
	/// </param>
	/// <param name="transformValue"></param>
	public void transform(String express, Function<Object, Object> transformValue) {
		AssignExpression exp = TypeUtil.as(AssignExpression.create(express), AssignExpression.class);
		if (exp == null)
			throw new IllegalArgumentException(Language.strings("ExpressionError", express));
		exp.execute(this, transformValue);
	}

//	/// <summary>
//	///
//	/// </summary>
//	/// <param name="listName"></param>
//	/// <param name="action"></param>
//	/// <param name="self">自身是否参与遍历</param>
//	public void DeepEach(String listName, Action<DTObject> action, bool self = false)
//	 {
//	     this.Each(listName, (child) =>
//	     {
//	         child.DeepEach(listName, action);
//	         action(child);
//	     });
//	     if (self) action(this);
//	 }

//	 #endregion

//	#region 空对象

	private final static String EmptyCode = "{__empty:true}";

	public static final DTObject Empty = DTObject.readonly(EmptyCode);

	public boolean isEmpty() {
		return this.getBoolean("__empty", false);
	}

//	#endregion

	static DTObject obtain(DTEObject root, boolean readonly) {
		return ContextSession.registerItem(new DTObject(root, readonly));
	}

	static DTObject obtain() {
		return ContextSession.registerItem(new DTObject(DTEObject.obtainEditable(StringUtil.empty()), false));
	}

}
