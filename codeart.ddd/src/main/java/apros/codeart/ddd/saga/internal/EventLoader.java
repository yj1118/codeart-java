package apros.codeart.ddd.saga.internal;

import java.util.ArrayList;

import com.google.common.collect.Iterables;

import apros.codeart.AppConfig;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.runtime.Activator;
import apros.codeart.util.SafeAccessImpl;

public final class EventLoader {
	private EventLoader() {
	}

	private static Iterable<DomainEvent> _events;

	public static Iterable<DomainEvent> events() {
		return _events;
	}

	public static DomainEvent find(String name) {
		return Iterables.find(_events, (e) -> {
			return e.name().equals(name);
		});
	}

	/**
	 * 加载所有领域对象的元数据
	 */
	public static void load() {
		_events = findEvents();
	}

	private static Iterable<DomainEvent> findEvents() {
		var findedTypes = Activator.getSubTypesOf(DomainEvent.class, AppConfig.mergeArchives("subsystem"));
		ArrayList<DomainEvent> events = new ArrayList<>(Iterables.size(findedTypes));
		for (var findedType : findedTypes) {
			var event = SafeAccessImpl.createSingleton(findedType);
			events.add(event);
		}
		return events;
	}

}
