package apros.codeart.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;

import apros.codeart.context.ContextSession;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.ListUtil;

final class DTEList extends DTEntity implements Iterable<DTObject> {
	@Override
	public DTEntityType getType() {
		return DTEntityType.LIST;
	}

	private DTObject _template;

	private ArrayList<DTObject> _items;

	public Iterable<DTObject> getItems() {
		return _items;
	}

	private void addItem(DTObject item) {
		item.setParent(this);
		_items.add(item);
	}

	private boolean removeItem(DTObject item) {
		if (_items.remove(item)) {
			item.setParent(null);
			return true;
		}
		return false;
	}

	private void setItems(ArrayList<DTObject> items) throws Exception {
		_items = DTObject.obtainList();

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

	private DTEList() {

	}

	@Override
	public DTEntity cloneImpl() throws Exception {
		var obj = obtain();
		obj.setName(this.getName());
		obj._template = _template.clone();

		for (var item : this.getItems()) {
			obj.addItem(item.clone());
		}
		return obj;
	}

	@Override
	public boolean hasData() {
		if (_items == null)
			return false;
		for (var item : _items)
			if (item.hasData())
				return true;
		return false;
	}

	@Override
	public void clearData() {
		if (_items != null) {
			_items.clear();
		}
	}

	@Override
	public Iterator<DTObject> iterator() {
		return _items.iterator();
	}

	@Override
	public void setMember(QueryExpression query, Function<String, DTEntity> createEntity) throws Exception {
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
	public Iterable<DTEntity> finds(QueryExpression query) throws Exception {
		if (query.onlySelf())
			return this.getSelfAsEntities(); // *代表返回对象自己
		var list = DTEntity.obtainList();
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
		var temps = DTObject.obtainList();
		for (var i : indexs) {
			temps.add(_items.get(i));
		}
		// 只是内部项的移除，所以直接赋值就可以了，项的关系都还在
		_items = temps;
	}

	public void removeAts(Iterable<Integer> indexs) throws Exception {
		var temps = DTObject.obtainList();
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

	public Boolean remove(Function<DTObject, Boolean> predicate) {
		var item = ListUtil.find(_items, predicate);
		if (item != null) {
			removeItem(item);
			return true;
		}
		return false;
	}

	/// <summary>
	/// 新建一个子项，并将新建的子项加入集合中
	/// </summary>
	/// <returns></returns>
	public void CreateAndPush(Action<DTObject> fill) {
		DTObject obj = this.ItemTemplate.Clone();
		Items.Add(obj);
		if (fill != null)
			fill(obj);
		this.Changed();
	}

	public DTObject CreateAndPush() {
		DTObject obj = this.ItemTemplate.Clone();
		Items.Add(obj);
		this.Changed();
		return obj;
	}

	public void CreateAndInsert(int index, Action<DTObject> fill) {
		DTObject obj = this.ItemTemplate.Clone();
		Items.Insert(index, obj);
		if (fill != null)
			fill(obj);
		this.Changed();
	}

	public DTObject CreateAndInsert(int index) {
		DTObject obj = this.ItemTemplate.Clone();
		Items.Insert(index, obj);
		this.Changed();
		return obj;
	}

	public void Push(DTObject item) {
		Items.Add(item);
		this.Changed();
	}

	public void Insert(int index, DTObject item) {
		Items.Insert(index, item);
		this.Changed();
	}

	private DTObjects _objects;

	public DTObjects GetObjects() {
		if (_objects == null) {
			_objects = new DTObjects(Items);
		}
		return _objects;
	}

	public IEnumerable<T> GetValues<T>()
	{
	    return this.GetObjects().Select((t)=>t.GetValue<T>());
	}

	public int Count
	{
	    get
	    {
	        return Items.Count;
	    }
	}

	public DTObject this[
	int index]
	{
	    get
	    {
	        return Items[index];
	    }
	}

	public IEnumerator<DTObject> GetEnumerator() {
		return Items.GetEnumerator();
	}

	IEnumerator IEnumerable.GetEnumerator()
	{
	    return Items.GetEnumerator();
	}

	public override void Changed()
	{
	    _objects = null;
	    if (this.Parent != null)
	        this.Parent.Changed();
	}

	@Override
	public String getCode(boolean sequential, boolean outputName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSchemaCode(boolean sequential, boolean outputName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private static Pool<DTEList> pool = new Pool<DTEList>(() -> {
		return new DTEList();
	}, PoolConfig.onlyMaxRemainTime(300));

	public static DTEList obtain() throws Exception {
		var item = ContextSession.obtainItem(pool, () -> new DTEList());
		item.setItems(DTObject.obtainList());
		return item;
	}

	public static DTEList obtain(ArrayList<DTObject> items) throws Exception {
		var obj = ContextSession.obtainItem(pool, () -> new DTEList());
		obj.setItems(items);
		return obj;
	}

}
