package com.apros.codeart.util;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ObservableCollection<E> extends ArrayList<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5134725083126493940L;

	public ObservableCollection(Iterable<E> items) {
		if (items != null) {
			ListUtil.addRange(this, items);
		}
	}

	private final EventBus eventBus = new EventBus();

	public void addObserver(CollectionObserver observer) {
		eventBus.register(observer);
	}

	public void removeObserver(Object observer) {
		eventBus.unregister(observer);
	}

	@Override
	public boolean add(E e) {
		boolean added = super.add(e);
		if (added) {
			eventBus.post(new NotifyCollectionChangedEventArgs(this));
		}
		return added;
	}

	public static class CollectionObserver {

		private Consumer<NotifyCollectionChangedEventArgs> _action;

		public CollectionObserver(Consumer<NotifyCollectionChangedEventArgs> action) {
			_action = action;
		}

		@Subscribe
		public void onCollectionChange(NotifyCollectionChangedEventArgs event) {
			_action.accept(event);
		}
	}

}