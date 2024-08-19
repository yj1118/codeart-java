package apros.codeart.dto;

import static apros.codeart.i18n.Language.strings;
import static apros.codeart.runtime.TypeUtil.as;
import static apros.codeart.runtime.TypeUtil.is;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.util.Strings;

import com.google.common.collect.Iterables;

import apros.codeart.dto.serialization.IDTOSerializable;
import apros.codeart.dto.serialization.internal.DTObjectMapper;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.INullProxy;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;
import apros.codeart.util.TimeUtil;

/**
 * 本次升级，重写了底层算法，特点：
 * <p>
 * 1.在会话上下文结束后，会切断双向引用，避免内存泄露
 * <p>
 * 2.解析字符串后，各项成员不实际存放值，而是存放的值对应的字符串，当获取的时候，可以显示调用getInt等基元类型的操作，返回基元值，避免装箱。
 * <p>
 * 3.只读模式的dto底层用ArrayList存放数据，可编辑的dto用LinkedList存放数据
 * <p>
 * 我们基于这样的事实设计DTO：接受字符串，使用一次给调用方，或者将数据给与DTO，DOT生成JSON格式字符串，发送到网络。
 * <p>
 * 在这种模式下，避免了装箱和拆箱，性能良好。
 * <p>
 * 但是要频繁的操作dto的同样的值，比如getInt("value")或者setInt("value")调用好几遍，那么建议用getIntRef这种引用系方法。
 */
public class DTObject implements INullProxy, IDTOSchema {

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

    private boolean _isReadonly;

    public boolean isReadOnly() {
        return _isReadonly;
    }

    private void validateReadOnly() {
        if (_isReadonly)
            throw new DTOReadonlyException();
    }

    DTObject(DTEObject root, boolean isReadOnly) {
        _root = root;
        _isReadonly = isReadOnly;
    }

//	#region 值

    public Byte getByteRef(String findExp) {
        return this.getValue(findExp, null, true, DTEValue::getByteRef);
    }

    public Byte getByteRef(String findExp, Byte defaultValue) {
        return this.getValue(findExp, defaultValue, false, DTEValue::getByteRef);
    }

    public Short getShortRef(String findExp) {
        return this.getValue(findExp, null, true, DTEValue::getShortRef);
    }

    public Short getShortRef(String findExp, Short defaultValue) {
        return this.getValue(findExp, defaultValue, false, DTEValue::getShortRef);
    }

    public Integer getIntRef(String findExp) {
        return this.getValue(findExp, null, true, DTEValue::getIntRef);
    }

    public Integer getIntRef(String findExp, Integer defaultValue) {
        return this.getValue(findExp, defaultValue, false, DTEValue::getIntRef);
    }

    public Long getLongRef(String findExp) {
        return this.getValue(findExp, null, true, DTEValue::getLongRef);
    }

    public Long getLongRef(String findExp, Long defaultValue) {
        return this.getValue(findExp, defaultValue, false, DTEValue::getLongRef);
    }

    public Float getFloatRef(String findExp) {
        return this.getValue(findExp, null, true, DTEValue::getFloatRef);
    }

    public Float getFloatRef(String findExp, Float defaultValue) {
        return this.getValue(findExp, defaultValue, false, DTEValue::getFloatRef);
    }

    public Double getDoubleRef(String findExp) {
        return this.getValue(findExp, null, true, DTEValue::getDoubleRef);
    }

    public Double getDoubleRef(String findExp, Double defaultValue) {
        return this.getValue(findExp, defaultValue, false, DTEValue::getDoubleRef);
    }

    public Boolean getBooleanRef(String findExp) {
        return this.getValue(findExp, null, true, DTEValue::getBooleanRef);
    }

    public Boolean getBooleanRef(String findExp, boolean defaultValue) {
        return this.getValue(findExp, defaultValue, false, DTEValue::getBooleanRef);
    }

    public Character getCharRef(String findExp) {
        return this.getValue(findExp, null, true, DTEValue::getCharRef);
    }

    public Character getCharRef(String findExp, Character defaultValue) {
        return this.getValue(findExp, defaultValue, false, DTEValue::getCharRef);
    }

    public Iterable<String> getStrings(String findExp) {
        return this.getValues(String.class, findExp, null, true);
    }

    public Iterable<String> getStrings(String findExp, Supplier<Iterable<String>> getDefaultValue) {
        var list = this.getValues(String.class, findExp, null, false);
        return list == null ? getDefaultValue.get() : list;
    }

    public Iterable<String> getStrings(String findExp, boolean throwError) {
        return this.getValues(String.class, findExp, null, throwError);
    }

    public long[] getLongs(String findExp) {
        return getLongs(findExp, true);
    }

    public long[] getLongs(String findExp, boolean throwError) {
        DTEList entity = find(DTEList.class, findExp, throwError);
        if (entity == null)
            return null;
        return entity.getLongs();
    }

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
                    return DTObject.obtain(eo, _isReadonly);
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

    public Object getValue(String findExp, Class<?> type) {
        return getValue(findExp, type, true);
    }

    public Object getValue(String findExp, Class<?> type, boolean throwError) {
        DTEntity entity = find(findExp, throwError);
        if (entity == null) return null;

        var ev = as(entity, DTEValue.class);
        if (ev == null) return null;

        return ev.getValueRef(type);
    }

    <T> T getValue(String findExp, T defaultValue, boolean throwError, Function<DTEValue, T> extractValue) {
        DTEntity entity = find(findExp, throwError);
        if (entity == null) return null;

        var ev = as(entity, DTEValue.class);
        if (ev == null) return null;
        return extractValue.apply(ev);
    }

    public DTObject getObject(String findExp) {
        return getObject(findExp, null, true);
    }

    public DTObject getObject(String findExp, DTObject defaultValue) {
        return getObject(findExp, defaultValue, false);
    }

    private DTObject getObject(String findExp, DTObject defaultValue, boolean throwError) {
        var entity = this.find(DTEObject.class, findExp, throwError);
        return entity == null ? defaultValue : DTObject.obtain(entity, this.isReadOnly());
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

    public boolean getBoolean() {
        return getBoolean(StringUtil.empty(), false, true);
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

    public byte getByte() {
        return getByte(StringUtil.empty(), (byte) 0, true);
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

    public char getChar() {
        return getChar(StringUtil.empty(), StringUtil.charEmpty(), true);
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

    public short getShort() {
        return getShort(StringUtil.empty(), (short) 0, true);
    }

    public int getInt(String findExp, int defaultValue) {
        return getInt(findExp, defaultValue, false);
    }

    public int getInt(String findExp) {
        return getInt(findExp, 0, true);
    }

    public int getInt() {
        return getInt(StringUtil.empty(), 0, true);
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

    public long getLong() {
        return getLong(StringUtil.empty(), 0L, true);
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

    public float getFloat() {
        return getFloat(StringUtil.empty(), 0F, true);
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

    public double getDouble() {
        return getDouble(StringUtil.empty(), 0D, true);
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

    public ZonedDateTime getZonedDateTime(String findExp, ZonedDateTime defaultValue) {
        return getZonedDateTime(findExp, defaultValue, false);
    }

    public ZonedDateTime getZonedDateTime(String findExp) {
        return getZonedDateTime(findExp, TimeUtil.MinZonedDateTime, true);
    }

    public ZonedDateTime getZonedDateTime(String findExp, ZonedDateTime defaultValue, boolean throwError) {
        DTEValue entity = find(DTEValue.class, findExp, throwError);
        return entity == null ? defaultValue : entity.getZonedDateTime();
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

    public void setByte(byte value) {
        setValue(StringUtil.empty(), Byte.toString(value), false);
    }

    public void setShort(String findExp, short value) {
        setValue(findExp, Short.toString(value), false);
    }

    public void setShort(short value) {
        setValue(StringUtil.empty(), Short.toString(value), false);
    }

    public void setInt(int value) {
        setValue(StringUtil.empty(), Integer.toString(value), false);
    }

    public void setInt(String findExp, int value) {
        setValue(findExp, Integer.toString(value), false);
    }

    public void setLong(String findExp, long value) {
        setValue(findExp, Long.toString(value), false);
    }

    public void setLong(long value) {
        setValue(StringUtil.empty(), Long.toString(value), false);
    }

    public void setFloat(String findExp, float value) {
        setValue(findExp, Float.toString(value), false);
    }

    public void setFloat(float value) {
        setValue(StringUtil.empty(), Float.toString(value), false);
    }

    public void setDouble(String findExp, double value) {
        setValue(findExp, Double.toString(value), false);
    }

    public void setDouble(double value) {
        setValue(StringUtil.empty(), Double.toString(value), false);
    }

    public void setBoolean(String findExp, boolean value) {
        setValue(findExp, Boolean.toString(value), false);
    }

    public void setBoolean(boolean value) {
        setValue(StringUtil.empty(), Boolean.toString(value), false);
    }

    public void setChar(String findExp, char value) {
        setValue(findExp, Character.toString(value), false);
    }

    public void setChar(char value) {
        setValue(StringUtil.empty(), Character.toString(value), false);
    }

    public void setString(String value) {
        setValue(StringUtil.empty(), value, true);
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
                return createPrimitiveEntity(name, valueCode, valueIsString);
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
                    throw new IllegalStateException(strings("apros.codeart", "DTOExpressionError", findExp));

                var query = QueryExpression.create(e.getName());
                parent.setMember(query, (name) -> {
                    return createPrimitiveEntity(name, valueCode, valueIsString);
                });
            }
        }
    }

    // endregion

    public void setLocalDateTime(String findExp, LocalDateTime value) {
        setValueRef(findExp, value, true); // 时间转换成json，会带""号，所以valueCodeIsString是true
    }

    public void setZonedDateTime(String findExp, ZonedDateTime value) {
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
                    throw new IllegalStateException(strings("apros.codeart", "DTOExpressionError", findExp));

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

    public void pushStrings(String findExp, String[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setString(value);
            entity.push(member);
        }
    }

    public void pushStrings(String findExp, Iterable<String> values) {
        pushValues(findExp, values);
    }

    private void pushValues(String findExp, Iterable<?> values) {
        validateReadOnly();

        getOrCreateList(findExp);// 以此来防止当items个数为0时，没有创建的bug
        if (values == null)
            return;
        for (var value : values) {
            DTObject item = DTObject.value(value);
            push(findExp, item);
        }
    }

    /**
     * 该方法会将obj克隆到当前dto中，obj的后续操作和当前dto的后续操作互不相关
     *
     * @param findExp
     * @param obj
     */
    public void setObject(String findExp, DTObject obj) {
        if (obj == null || obj.isEmpty())
            return;
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
     * 合并对象，注意，合并后 {@obj} 和 对当前对象共享数据，且 {@obj} 的父亲就变为 当前dto对象了
     * <p>
     * 该方法比 setObject 方法高效，但是会对{@obj}造成副作用，所以除非追求极致的性能且{@obj}的副作用确定不影响程序运行，否则不要使用
     *
     * @param findExp
     * @param obj
     */
    public void combineObject(String findExp, DTObject obj) {

        validateReadOnly();

        if (obj == null || obj.isEmpty())
            return;

        if (obj.isReadOnly()) {
            this.setObject(findExp, obj);
            return;
        }

        if (StringUtil.isNullOrEmpty(findExp)) {
            _root = (DTEObject) obj.getRoot();
        } else {
            var query = QueryExpression.create(findExp);
            _root.setMember(query, (name) -> {
                var e = (DTEObject) obj.getRoot();
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

    private DTEntity createPrimitiveEntity(String name, String valueCode, boolean valueIsString) {
        return DTEValue.obtainByCode(name, valueCode, valueIsString);
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
                throw new IllegalStateException(strings("apros.codeart", "DTOEntityNotFound", findExp));
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
            throw new IllegalStateException(strings("apros.codeart", "DTOMemberNotMatch", findExp, cls.getName()));
        return entity;
    }

    Iterable<DTEntity> finds(String findExp, boolean throwError) {
        var query = QueryExpression.create(findExp);

        var es = _root.finds(query);

        if (Iterables.size(es) == 0 && throwError) {
            throw new IllegalStateException(strings("apros.codeart", "DTOEntityNotFound", findExp));
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

    public Iterable<DTObject> getObjects(String findExp) {
        return getObjects(findExp, true);
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
    private <T> Iterable<T> getValues(Class<T> itemClass, String findExp, T itemDefaultValue, boolean throwError) {
        DTEList entity = find(DTEList.class, findExp, throwError);
        if (entity == null)
            return null;
        return entity.getValues(itemClass, itemDefaultValue, throwError);
    }

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

    private void push(String findExp, Iterable<DTObject> items) {
        validateReadOnly();

        getOrCreateList(findExp);// 以此来防止当items个数为0时，没有创建的bug
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

    public DTObject getElement(String findExp, int index, boolean throwError) {
        DTEList entity = find(DTEList.class, findExp, throwError);
        if (entity == null)
            return null;
        return entity.getElement(index);
    }

    /**
     * 向集合追加一个成员
     *
     * @param findExp
     * @param member
     */
    public void push(String findExp, DTObject member) {
        if (member == null)
            return;

        validateReadOnly();

        DTEList entity = getOrCreateList(findExp);
        entity.push(member);
    }

    public void pushByte(String findExp, byte value) {
        DTObject member = DTObject.editable();
        member.setByte(value);
        push(findExp, member);
    }

    public void pushBytes(String findExp, byte[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setByte(value);
            entity.push(member);
        }
    }

    public void pushShort(String findExp, short value) {
        DTObject member = DTObject.editable();
        member.setShort(value);
        push(findExp, member);
    }

    public void pushShorts(String findExp, short[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setShort(value);
            entity.push(member);
        }
    }

    public void pushInt(String findExp, int value) {
        DTObject member = DTObject.editable();
        member.setInt(value);
        push(findExp, member);
    }

    public void pushInts(String findExp, int[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setInt(value);
            entity.push(member);
        }
    }

    public void pushLong(String findExp, long value) {
        DTObject member = DTObject.editable();
        member.setLong(value);
        push(findExp, member);
    }

    public void pushLongs(String findExp, long[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setLong(value);
            entity.push(member);
        }
    }

    public void pushFloat(String findExp, float value) {
        DTObject member = DTObject.editable();
        member.setFloat(value);
        push(findExp, member);
    }

    public void pushFloats(String findExp, float[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setFloat(value);
            entity.push(member);
        }
    }

    public void pushDouble(String findExp, double value) {
        DTObject member = DTObject.editable();
        member.setDouble(value);
        push(findExp, member);
    }

    public <T> void pushObjects(String findExp, String rowSchemaCode, Iterable<T> objs) {
        var data = DTObject.editable(rowSchemaCode, objs);
        this.pushObjects(findExp, data.getList("rows"));
    }

    public void pushObjects(String findExp, Iterable<DTObject> values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            entity.push(value);
        }
    }

    public void pushDoubles(String findExp, double[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setDouble(value);
            entity.push(member);
        }
    }

    public void pushChar(String findExp, char value) {
        DTObject member = DTObject.editable();
        member.setChar(value);
        push(findExp, member);
    }

    public void pushChars(String findExp, char[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setChar(value);
            entity.push(member);
        }
    }

    public void pushBoolean(String findExp, boolean value) {
        DTObject member = DTObject.editable();
        member.setBoolean(value);
        push(findExp, member);
    }

    public void pushBooleans(String findExp, boolean[] values) {
        validateReadOnly();
        DTEList entity = getOrCreateList(findExp);
        for (var value : values) {
            DTObject member = DTObject.editable();
            member.setBoolean(value);
            entity.push(member);
        }
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

    /**
     * 以键值对的形式遍历对象
     *
     * @param action
     */
    public void each(BiConsumer<String, Object> action) {
        each(Strings.EMPTY, action);
    }

    /**
     * 以键值对的形式遍历对象，请注意 {@findExp} 对应的成员必须为对象类型
     *
     * @param findExp
     * @param action
     */
    public void each(String findExp, BiConsumer<String, Object> action) {
        var eo = TypeUtil.as(_root.find(findExp), DTEObject.class);
        if (eo == null) return;

        for (var member : eo.getMembers()) {
            var name = member.getName();
            var value = extractValueRef(member);
            action.accept(name, value);
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
        result.push("rows", data);
        return result;
    }

    public DTObject page(String findExp, int pageIndex, int pageSize) {
        var list = getList(findExp, false);
        int dataCount = list == null ? 0 : Iterables.size(list);

        var result = DTObject.editable();
        result.setInt("pageIndex", pageIndex);
        result.setInt("pageSize", pageSize);
        result.setInt("dataCount", dataCount);

        var start = pageIndex * pageSize;
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

        result.push(findExp, items);
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
        return obtain((DTEObject) _root.clone(), _isReadonly);
    }

    public DTObject clone(boolean readonly) {
        return obtain((DTEObject) _root.clone(), readonly);
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

    public DTObject asReadonly() {
        if (_isReadonly)
            return this;
        _isReadonly = true;
        _root.setReadonly(true);
        return this;
    }

    public DTObject asEditable() {
        if (!_isReadonly)
            return this;
        _isReadonly = false;
        _root.setReadonly(false);
        return this;
    }

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
     * 创建一个单值的dto
     *
     * @param value
     * @return
     */
    public static DTObject value(Object value) {
        // todo 以后可以优化性能
        var t = DTObject.editable();
        t.setValue(value);
        return t;
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

    public static DTObject readonly(byte[] bytes) {
        String message = new String(bytes, StandardCharsets.UTF_8);
        return DTObject.readonly(message);
    }

    public static <T> DTObject readonly(String rowSchemaCode, Iterable<T> objs) {
        var result = DTObject.editable();

        if (objs == null) {
            result.setValue("dataCount", 0);
            result.push("rows");
        } else {
            result.setValue("dataCount", Iterables.size(objs));
            result.push("rows", objs, (obj) ->
            {
                return DTObject.readonly(rowSchemaCode, obj);
            });
        }
        return result.asReadonly();
    }

    public static DTObject editable(String schemaCode, Object target) {
        return createImpl(schemaCode, target, false);
    }

    public static DTObject editable(Object target) {
        return createImpl(StringUtil.empty(), target, false);
    }

    public static DTObject editable(byte[] bytes) {
        String message = new String(bytes, StandardCharsets.UTF_8);
        return DTObject.editable(message);
    }

    public static <T> DTObject editable(String rowSchemaCode, Iterable<T> objs) {
        var result = DTObject.editable();
        if (objs == null) {
            result.setValue("dataCount", 0);
            result.push("rows");
        } else {
            result.setValue("dataCount", Iterables.size(objs));
            result.push("rows", objs, (obj) ->
            {
                return DTObject.editable(rowSchemaCode, obj);
            });
        }

        return result;
    }

    /**
     * 根据架构代码将对象的信息加载到dto中
     *
     * @param schemaCode
     * @param target
     * @return
     */
    static DTObject createImpl(String schemaCode, Object target, boolean isReadonly) {

        var dy = as(target, IDTOSerializable.class);
        if (dy != null)
            return dy.getData(schemaCode);

        var dto = as(target, DTObject.class);
        if (dto != null) {
            DTObject result = isReadonly ? DTObject.readonly() : DTObject.editable();
            result.load(schemaCode, dto);
            return result;
        } else {
            DTObject result = DTObjectMapper.load(schemaCode, target);
            if (isReadonly)
                result.asReadonly();
            return result;
        }

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
     * 根据架构代码，将dto中的数据全部保存到 obj 实例中
     *
     * @param obj
     * @param schemaCode
     */
    public void save(Object obj, String schemaCode) {
        DTObjectMapper.save(obj, schemaCode, this);
    }

    /// <summary>
    /// 将dto的内容保存到 instance 里
    /// </summary>
    /// <param name="instance"></param>
    public void save(Object instance) {
        DTObjectMapper.save(instance, this);
    }

    // 将dto对象反序列化到一个实体对象中
    public <T> T save(Class<T> cls) {
        return DTObject.save(cls, this);
    }

    @SuppressWarnings("unchecked")
    public static <T> T save(Class<T> objectType, DTObject dto) {
        return (T) DTObjectMapper.save(objectType, dto);
    }

    /**
     * 根据架构代码将对象的信息加载到dto中
     *
     * @param schemaCode
     * @param target
     */
    public void load(String schemaCode, Object target) {
        var dy = TypeUtil.as(target, IDTOSerializable.class);
        if (dy != null) {
            load(schemaCode, dy.getData(schemaCode));
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
                    var t = (DTEntity) te.clone();
                    t.setName(name);    //防止名称大小写不同，这里要设置名称
                    this._root.addMember(t);
                }
            }
        }
    }

    public void loadBy(Object target) {
        load(StringUtil.empty(), target);
    }

    /**
     * 将 obj 里面的所有属性的值加载到dto中
     *
     * @param obj
     * @return
     */
    public static DTObject load(Object obj) {
        return DTObjectMapper.load(obj);
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
            throw new IllegalArgumentException(Language.strings("apros.codeart", "ExpressionError", express));
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

    public byte[] toBytes() {
        var code = this.getCode();
        return code.getBytes(StandardCharsets.UTF_8);
    }


//	#region 空对象

    private final static String EmptyCode = "{__empty:true}";

    public static final DTObject Empty = DTObject.readonly(EmptyCode);

    public static DTObject empty() {
        return Empty;
    }

    public boolean isEmpty() {
        return _root.isEmpty() || this.getBoolean("__empty", false);
    }

    public boolean isNull() {
        return this.isEmpty();
    }

//	#endregion

    static DTObject obtain(DTEObject root, boolean readonly) {
        return new DTObject(root, readonly);
//		return AppSession.registerItem(new DTObject(root, readonly));
    }

    static DTObject obtain() {
        return new DTObject(DTEObject.obtainEditable(StringUtil.empty()), false);
//		return AppSession.registerItem(new DTObject(DTEObject.obtainEditable(StringUtil.empty()), false));
    }

    @Override
    public String toString() {
        return this.getCode();
    }

    @Override
    public String matchChildSchema(String name) {
        if (this.isEmpty()) return name; //没有写任何值的架构，就是全部输出
        var e = this.find(name, false);
        return e == null ? null : e.getName();
    }

    @Override
    public IDTOSchema getChildSchema(String name) {
        if (this.isEmpty()) return DTObject.Empty;
        return find(name, true);
    }

    @Override
    public Iterable<String> getSchemaMembers() {
        return this._root.getSchemaMembers();
    }
}
