package apros.codeart.ddd.message;

import apros.codeart.util.SafeAccessImpl;

public final class MessageLogFactory {
	private MessageLogFactory() {
	}

	private static class MessageLogFactoryHolder {
		static final IMessageLogFactory factory;

		private static IMessageLogFactory createFactory() {
			IMessageLogFactory temp = null;
			var impl = MessageConfig.logFactoryImplementer();
			if (impl != null) {
				temp = impl.getInstance(IMessageLogFactory.class);
			} else if (_registerFactory != null)
				temp = _registerFactory;
			else
				temp = FileMessageLogFactory.instance;

			SafeAccessImpl.checkUp(temp);
			return temp;
		}

		static {
			factory = createFactory();
		}

	}

	public static IMessageLog createLog() {
		return MessageLogFactoryHolder.factory.create();
	}

	public static IMessageLogFactory getFactory() {
		return MessageLogFactoryHolder.factory;
	}

	private static IMessageLogFactory _registerFactory;

	public static void register(IMessageLogFactory factory) {
		_registerFactory = factory;
	}

}
