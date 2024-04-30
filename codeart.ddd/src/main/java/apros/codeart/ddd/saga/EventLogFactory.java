package apros.codeart.ddd.saga;

import apros.codeart.util.SafeAccessImpl;

public final class EventLogFactory {
	private EventLogFactory() {
	}

	private static class EventLogFactoryHolder {
		static final IEventLogFactory factory;

		private static IEventLogFactory createFactory() {
			IEventLogFactory temp = null;
			var impl = SAGAConfig.eventLogFactoryImplementer();
			if (impl != null) {
				temp = impl.getInstance(IEventLogFactory.class);
			} else if (_registerFactory != null)
				temp = _registerFactory;
			else
				temp = FileEventLogFactory.instance;

			SafeAccessImpl.checkUp(temp);
			return temp;
		}

		static {
			factory = createFactory();
		}

	}

	public static IEventLog getLog() {
		return EventLogFactoryHolder.factory.create();
	}

	private static IEventLogFactory _registerFactory;

	public static void register(IEventLogFactory factory) {
		_registerFactory = factory;
	}

}
