package com.apros.codeart.dto;

import static com.apros.codeart.util.StringUtil.isNullOrEmpty;
import static com.apros.codeart.util.StringUtil.removeLast;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.apros.codeart.context.ContextSession;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

final class DTEList extends DTEntity implements Iterable<DTObject> {

	private boolean _isReadOnly;

	@Override
	public DTEntityType getType() {
		return DTEntityType.LIST;
	}

	private DTObject _template;

	private AbstractList<DTObject> _items;

	public Iterable<DTObject> getItems() {
		return _items;
	}

	private void addItem(DTObject item) {
		item.setParent(this);
		_items.add(item);
	}

	private void insertItem(int index, DTObject item) {
		item.setParent(this);
		_items.add(index, item);
	}

	private boolean removeItem(DTObject item) {
		if (_items.remove(item)) {
			item.setParent(null);
			return true;
		}
		return false;
	}

	private void setItems(AbstractList<DTObject> items) {
		_items = Util.createList(_isReadOnly, items.size());

		if (items.size() == 0) {
			this._template = DTObject.obtain();
			return;
		}

		if (items.size() == 1) {
			DTObject item = items.get(0);
			if (item.hasData())
				addItem(item);
			this._template = item.clone();
		} else {
			for (var item : items) {
				addItem(item);
			}
			this._template = _items.get(0).clone();
		}
		this._template.forceClearData();
	}

	private DTEList(boolean isReadOnly, String name, AbstractList<DTObject> items) {
		_isReadOnly = isReadOnly;
		this.setName(name);
		this.setItems(items);
	}

	@Override
	public DTEntity cloneImpl() {

		var length = _items.size();

		var obj = obtain(_isReadOnly, this.getName(), Util.createList(_isReadOnly, length));
		obj._template = _template.clone();

		for (var item : _items) {
			obj.addItem(item.clone());
		}
		return obj;
	}

	@Override
	public boolean hasData() {
		for (var item : _items)
			if (item.hasData())
				return true;
		return false;
	}

	@Override
	public void close() throws Exception {
		super.close();
		_template = null;
		_items = null; // 项会在会话结束后自动关闭
	}

	@Override
	public void clearData() {
		_items.clear();
	}

	@Override
	public Iterator<DTObject> iterator() {
		return _items.iterator();
	}

	@Override
	public void setMember(QueryExpression query, Function<String, DTEntity> createEntity) {
		this._template.getRoot().setMember(query, createEntity);
		this._template.clearData();

		for (var item : _items) {
			item.getRoot().setMember(query, createEntity);
		}
	}

	@Override
	public void removeMember(DTEntity e) {
		DTObject taget = null;
		for (var child : _items) {
			if (child.getRoot() == e) {
				taget = child;
				break;
			}
		}
		if (taget != null)
			removeItem(taget);
	}

	@Override
	public Iterable<DTEntity> finds(QueryExpression query) {
		if (query.onlySelf())
			return this.getSelfAsEntities(); // *代表返回对象自己
		var list = new ArrayList<DTEntity>();
		for (var e : _items) {
			var es = e.getRoot().finds(query);
			ListUtil.addRange(list, es);
		}
		return list;
	}

//	private IList<DTObject> GetItems(IList<int> indexs)
//	{
//	    var items = new List<DTObject>();
//	    for (var i = 0; i < indexs.Count; i++)
//	    {
//	        var pointer = indexs[i];
//	        if (pointer >= 0 && pointer < _list.Count)
//	            items.Add(_list[pointer]);
//	    }
//	    return items;
//	}

	/**
	 * 保留指定索引的项
	 * 
	 * @param indexs
	 * @throws Exception
	 */
	public void retainAts(Iterable<Integer> indexs) throws Exception {
		var temps = Util.<DTObject>createList(_isReadOnly, Iterables.size(indexs));
		for (var i : indexs) {
			temps.add(_items.get(i));
		}
		// 只是内部项的移除，所以直接赋值就可以了，项的关系都还在
		_items = temps;
	}

	public void removeAts(Iterable<Integer> indexs) {
		int length = _items.size() - Iterables.size(indexs);
		var temps = Util.<DTObject>createList(_isReadOnly, length);
		for (var i = 0; i < _items.size(); i++) {
			if (!ListUtil.contains(indexs, i)) { // 在移除的索引之外的项，保留
				temps.add(_items.get(i));
			}
		}
		// 只是内部项的移除，所以直接赋值就可以了，项的关系都还在
		_items = temps;
	}

	public void removeAt(int index) {
		var item = _items.get(index);
		removeItem(item);
	}

	public boolean remove(Function<DTObject, Boolean> predicate) {
		var item = ListUtil.find(_items, predicate);
		if (item != null) {
			removeItem(item);
			return true;
		}
		return false;
	}

	/**
	 * 新建一个子项，并将新建的子项加入集合中
	 * 
	 * @param fill
	 */
	public void push(Consumer<DTObject> fill) {
		DTObject obj = _template.clone();
		addItem(obj);
		if (fill != null)
			fill.accept(obj);
	}

	public DTObject push() {
		DTObject obj = _template.clone();
		addItem(obj);
		return obj;
	}

	public void push(int index, Consumer<DTObject> fill) {
		DTObject obj = _template.clone();
		insertItem(index, obj);
		if (fill != null)
			fill.accept(obj);
	}

	public DTObject push(int index) {
		DTObject obj = _template.clone();
		insertItem(index, obj);
		return obj;
	}

	public void push(DTObject item) {
		addItem(item);
	}

	public void push(int index, DTObject item) {
		insertItem(index, item);
	}

	public List<DTObject> getObjects() {
		return Collections.unmodifiableList(_items);
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> getValues(Class<T> cls, T defaultValue, boolean throwError) {
		return ListUtil.map(_items, (t) -> {
			return (T) t.getValue(StringUtil.empty(), defaultValue, throwError);
		});
	}

	public int size() {
		return _items.size();
	}

	public DTObject get(int index) {
		return _items.get(index);
	}

	@Override
	public void fillCode(StringBuilder code, boolean sequential, boolean outputName) {
		var name = this.getName();
		if (outputName && !isNullOrEmpty(name))
			code.append(String.format("\"%s\"", name));

		if (code.length() > 0)
			code.append(":");
		code.append("[");
		for (DTObject item : _items) {
			var itemCode = item.getCode(sequential, false);
			code.append(itemCode);
			code.append(",");
		}
		if (_items.size() > 0)
			removeLast(code);
		code.append("]");
	}

	@Override
	public void fillSchemaCode(StringBuilder code, boolean sequential, boolean outputName) {
		var name = this.getName();
		if (outputName && !isNullOrEmpty(name))
			code.append(String.format("\"%s\"", name));

		if (code.length() > 0)
			code.append(":");
		code.append("[");
		code.append(this._template.getSchemaCode(sequential, false));
		code.append("]");
	}

	/**
	 * 获取一个可编辑的实例，该方法用于dto被调用写操作时
	 * 
	 * @param name
	 * @return
	 */
	public static DTEList obtainEditable(String name) {
		return ContextSession.registerItem(new DTEList(false, name, new LinkedList<DTObject>()));
	}

	public static DTEList obtain(boolean isReadOnly, String name, AbstractList<DTObject> items) {
		return ContextSession.registerItem(new DTEList(isReadOnly, name, items));
	}

}
