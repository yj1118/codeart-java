package apros.codeart.ddd.message;

import apros.codeart.util.SafeAccessImpl;

public final class MessageLogFactory {
	private MessageLogFactory() {
	}

	private static class MessageLogFactoryHolder {
		static final IMessageLogFactory Factory;

		private static IMessageLogFactory createFactory() {
			IMessageLogFactory temp = null;
			var impl = MessageConfig.logFactoryImplementer();
			if (impl != null) {
				temp = impl.getInstance(IMessageLogFactory.class);
			} else if (_registerFactory != null)
				temp = _registerFactory;
			else
				temp = FileMessageLogFactory.Instance;

			SafeAccessImpl.checkUp(temp);
			return temp;
		}

		static {
			Factory = createFactory();
		}

	}

	public static IMessageLog createLog() {
		return MessageLogFactoryHolder.Factory.create();
	}

	public static IMessageLogFactory getFactory() {
		return MessageLogFactoryHolder.Factory;
	}

	private static IMessageLogFactory _registerFactory;

	public static void register(IMessageLogFactory factory) {
		_registerFactory = factory;
	}

}
