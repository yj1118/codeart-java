package apros.codeart.ddd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import apros.codeart.dto.DTObjects;
import com.google.common.collect.Iterables;

import apros.codeart.dto.DTObject;
import apros.codeart.util.GUID;
import apros.codeart.util.StringUtil;

public class MapData implements Iterable<Map.Entry<String, Object>> {

    // 保存对象动态定义的属性值
    private final HashMap<String, Object> _data;

    public boolean containsKey(String key) {
        return _data.containsKey(key);
    }

    public boolean remove(String key) {
        return _data.remove(key) != null;
    }

    public Object get(String key) {
        return _data.get(key);
    }

    public Set<String> keys() {
        return _data.keySet();
    }

    public Collection<Object> values() {
        return _data.values();
    }

    public int size() {
        return _data.size();
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public void put(Map.Entry<String, Object> item) {
        _data.put(item.getKey(), item.getValue());
    }

    public Object put(String name, Object value) {
        return _data.put(name, value);
    }

    public void combine(MapData data) {
        for (var p : _data.entrySet()) {
            if (!this.containsKey(p.getKey()))
                this.put(p);
        }
    }

    public void update(MapData data) {
        for (var p : _data.entrySet()) {
            _data.put(p.getKey(), p.getValue());
        }
    }

    public void clear() {
        _data.clear();
    }

    public Iterator<Map.Entry<String, Object>> iterator() {
        return _data.entrySet().iterator();
    }

    public MapData() {
        _data = new HashMap<String, Object>();
    }

    /**
     * 尝试添加参数，当 {@code value} 为空时候不添加
     *
     * @param name
     * @param value
     */
    public void tryPut(String name, String value) {
        if (StringUtil.isNullOrEmpty(value))
            return;
        _data.put(name, value);
    }

    public void tryPut(String name, UUID value) {
        if (GUID.isNullOrEmpty(value))
            return;
        _data.put(name, value);
    }

    public void tryPut(String name, Object value) {
        if (value == null)
            return;
        _data.put(name, value);
    }

    public void tryPut(String name, Iterable<?> value) {
        if (value == null || Iterables.size(value) == 0)
            return;
        _data.put(name, value);
    }

    public DTObject asDTO() {
        DTObject dto = DTObject.editable();
        for (var p : _data.entrySet()) {
            dto.setValue(p.getKey(), p.getValue());
        }
        return dto;
    }

    public static DTObjects asDTO(Iterable<MapData> data) {
        DTObjects dto = new DTObjects();
        for (var item : data) {
            dto.add(item.asDTO());
        }
        return dto;
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return _data.entrySet();
    }


    public static final MapData EMPTY = new MapData();

}
