package apros.codeart.ddd;

import static apros.codeart.i18n.Language.strings;

import java.util.function.Supplier;

import apros.codeart.ddd.repository.DataContext;

public final class DomainBuffer {
	private DomainBuffer() {
	}

	public static void add(IAggregateRoot root) {
		add(root, false);
	}

	public static void add(IAggregateRoot root, boolean isMirror) {
		if (DataContext.existCurrent())
			DataContext.getCurrent().addBuffer(root, isMirror); // 加入数据上下文缓冲区
	}

	public static void remove(Class<?> objectType, Object id) {
		if (DataContext.existCurrent())
			DataContext.getCurrent().removeBuffer(objectType, id);
	}

	public static IAggregateRoot obtain(Class<?> objectType, Object id, Supplier<IAggregateRoot> load,
			boolean isMirror) {
		if (DataContext.existCurrent())
			return DataContext.getCurrent().obtainBuffer(objectType, id, load, isMirror);

		throw new IllegalStateException(strings("codeart.ddd", "UnknownException"));
	}

}
