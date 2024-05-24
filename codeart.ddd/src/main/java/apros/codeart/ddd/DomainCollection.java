package apros.codeart.ddd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.IEventObserver;

public class DomainCollection<E> extends ArrayList<E>
		implements IDomainCollection, IEventObserver<DomainObjectChangedEventArgs> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1243664948610752956L;

	private Class<E> _elementType;

	public Class<E> elementType() {
		return _elementType;
	}

	private DomainObject _parent;

	public DomainObject getParent() {
		return _parent;
	}

	public void setParent(DomainObject parent) {
		_parent = parent;
	}

	private String _propertyNameInParent;

	/**
	 * 集合在父对象中所担当的属性的名称
	 * 
	 * @return
	 */
	public String propertyNameInParent() {
		return _propertyNameInParent;
	}

	public DomainCollection(Class<E> elementType, DomainProperty propertyInParent) {
		this(elementType, propertyInParent, null);
	}

	public DomainCollection(Class<E> elementType, DomainProperty propertyInParent, Iterable<E> items) {
		this(elementType, propertyInParent.name(), items);
	}

	public DomainCollection(Class<E> elementType, String propertyNameInParent) {
		this(elementType, propertyNameInParent, null);
	}

	public DomainCollection(Class<E> elementType, String propertyNameInParent, Iterable<E> items) {
		if (items != null) {
			for (var item : items)
				this.add(item);
		}
		_propertyNameInParent = propertyNameInParent;
		_elementType = elementType;
	}

	@Override
	public Object clone() {
		return new DomainCollection<E>(_elementType, _propertyNameInParent, this);
	}

	@Override
	public E set(int index, E element) {
		var oldValue = super.set(index, element);
		if (oldValue != null)
			unbindChanged(element);
		bindChanged(element);
		return oldValue;
	}

	@Override
	public boolean add(E e) {
		boolean added = super.add(e);
		if (added) {
			bindChanged(e);
		}
		return added;
	}

	@Override
	public void add(int index, E element) {
		super.add(index, element);
		bindChanged(element);
	}

	@Override
	public E remove(int index) {
		var oldValue = super.remove(index);
		if (oldValue != null)
			unbindChanged(oldValue);
		return oldValue;
	}

	@Override
	public E removeFirst() {
		var oldValue = super.removeFirst();
		if (oldValue != null)
			unbindChanged(oldValue);
		return oldValue;
	}

	@Override
	public E removeLast() {
		var oldValue = super.removeLast();
		if (oldValue != null)
			unbindChanged(oldValue);
		return oldValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		var removed = super.remove(o);
		if (removed)
			unbindChanged((E) o);
		return removed;
	}

	@Override
	public void clear() {
		for (var o : this)
			unbindChanged(o);
		super.clear();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		var added = super.addAll(c);
		if (added) {
			for (var o : this)
				bindChanged(o);
		}
		return added;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		var added = super.addAll(index, c);
		if (added) {
			for (var o : this)
				bindChanged(o);
		}
		return added;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		var removed = super.removeAll(c);
		if (removed) {
			for (var o : this)
				unbindChanged(o);
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		for (var o : this) {
			if (!c.contains(o))
				unbindChanged(o);
		}

		return super.retainAll(c);
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {

		for (var o : this) {
			if (filter.test(o))
				unbindChanged(o);
		}

		return super.removeIf(filter);
	}

	private void bindChanged(E item) {
		var obj = TypeUtil.as(item, DomainObject.class);
		if (obj != null) {
			obj.changed().add(this);
		}
	}

	private void unbindChanged(E item) {
		var obj = TypeUtil.as(item, DomainObject.class);
		if (obj != null) {
			obj.changed().remove(this);
		}
	}

	private void markChanged() {
		if (this.getParent() == null)
			return;
		if (this.getParent().isConstructing())
			return; // 构造时不用标记
		this.getParent().markPropertyChanged(_propertyNameInParent);
	}

	@Override
	public void handle(Object sender, DomainObjectChangedEventArgs args) {
		markChanged();
	}

//	public static Object create(Class<?> elementType, DomainProperty propertyInParent) {
//		try (var cg = ClassGenerator.define()) {
//
//			try (var mg = cg.defineMethodPublicStatic("getList", List.class)) {
//				mg.newList().asReadonlyList();
//			}
//
//			var cls = cg.toClass();
//
//			var method = cls.getDeclaredMethod("getList");
//			return method.invoke(null);
//		} catch (Exception e) {
//			throw propagate(e);
//		}
//	}

}
