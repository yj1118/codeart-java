package com.apros.codeart.util;

public interface IEventObserver<E> {
	void handle(Object sender, E args);
}
