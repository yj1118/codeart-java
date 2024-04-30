package apros.codeart.ddd.saga;

import static apros.codeart.i18n.Language.strings;

import apros.codeart.ddd.DDDConfig;
import apros.codeart.util.SafeAccessImpl;

public final class EventLogFactory {
	private EventLogFactory() {
	}

	private static class EventLogFactoryHolder {
		static final IEventLog log;

		private static IEventLog createLog() {
			IEventLog log = null;
			var impl = DDDConfig.eventLogFactoryImplementer();
			if (impl != null) {
				var factory = impl.getInstance(IEventLogFactory.class);
				log = factory.create();
			} else if (_factory != null)
				log = _factory.create();
			else
				throw new IllegalStateException(strings("codeart.ddd", "UnknownException"));

			SafeAccessImpl.checkUp(log);
			return log;
		}

		static {
			log = createLog();
		}

	}

	public static IEventLog getLog() {
		return EventLogFactoryHolder.log;
	}

	private static IEventLogFactory _factory;

	public static void register(IEventLogFactory factory) {
		_factory = factory;
	}

}
