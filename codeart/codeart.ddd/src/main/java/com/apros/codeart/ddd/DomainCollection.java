package com.apros.codeart.ddd;

import com.apros.codeart.util.NotifyCollectionChangedEventArgs;
import com.apros.codeart.util.ObservableCollection;

public class DomainCollection<TMember> extends ObservableCollection<TMember> implements IDomainCollection {

	private DomainObject _parent;

	public DomainObject getParent() {
		return _parent;
	}

	public void setParent(DomainObject parent) {
		_parent = parent;
	}

	private DomainProperty _propertyInParent;

	/**
	 * 集合在父对象中所担当的属性定义
	 * 
	 * @return
	 */
	public DomainProperty propertyInParent() {
		return _propertyInParent;
	}

	private CollectionObserver _observer;

	public DomainCollection(DomainProperty propertyInParent) {
		this(propertyInParent, null);
	}

	public DomainCollection(DomainProperty propertyInParent, Iterable<TMember> items) {
		super(items);
		_propertyInParent = propertyInParent;
		_observer = new CollectionObserver((e) -> {
			onCollectionChanged(e);
		});
		this.addObserver(_observer);
	}

	/**
	 * 当集合发生改变时，通知父对象
	 * 
	 * @param e
	 */
	private void onCollectionChanged(NotifyCollectionChangedEventArgs e) {
		markChanged();
		SetMembersEvent(e);// 执行该代码后，成员对象发生改变，也会引起所在集合所在的属性发生改变的事件
	}

	private void markChanged() {
		if (this.getParent() == null)
			return;
		if (this.getParent().isConstructing())
			return; // 构造时不用标记
		this.getParent().markPropertyChanged(_propertyInParent);
	}

	private static void setMembersEvent(NotifyCollectionChangedEventArgs e)
	 {
	     var oldItems = e.OldItems;
	     if (oldItems != null)
	     {
	         foreach (var item in oldItems)
	         {
	             var obj = item as DomainObject;
	             if (obj != null)
	             {
	                 obj.Changed -= OnMemberChanged;
	             }
	         }
	     }

	     var newItems = e.NewItems;
	     if (newItems != null)
	     {
	         foreach (var item in newItems)
	         {
	             var obj = item as DomainObject;
	             if (obj != null)
	             {
	                 obj.Changed -= OnMemberChanged;
	                 obj.Changed += OnMemberChanged;
	             }
	         }
	     }
	 }

	private void OnMemberChanged(object sender, DomainObjectChangedEventArgs e) {
		MarkChanged();
	}

}
