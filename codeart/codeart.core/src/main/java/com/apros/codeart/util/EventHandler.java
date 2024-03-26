package com.apros.codeart.util;

import java.util.ArrayList;
import java.util.function.Supplier;

public class EventHandler<T> {

	private ArrayList<IEventObserver<T>> _observers;

	public EventHandler() {

	}

	public void add(IEventObserver<T> observer) {
		if (_observers == null)
			_observers = new ArrayList<IEventObserver<T>>();

		if (_observers.contains(observer))
			return;

		_observers.add(observer);
	}

	public void remove(IEventObserver<T> observer) {
		if (_observers == null)
			return;

		_observers.remove(observer);
	}

	public void raise(Object sender, Supplier<T> getArags) {
		if (_observers == null || _observers.size() == 0)
			return;

		var args = getArags.get();
		for (var observer : _observers) {
			observer.handle(sender, args);
		}
	}

}
