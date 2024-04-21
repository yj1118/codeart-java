package apros.codeart.mq;

import apros.codeart.AppConfig;
import apros.codeart.InterfaceImplementer;
import apros.codeart.mq.event.EventConfig;

public final class MQ {
	private MQ() {
	}

	private static class MQHolder {

		private static final EventConfig Event;

		static {

			Event = new EventConfig();

			var mq = AppConfig.section("mq");
			if (mq != null) {
				var eventNode = mq.getObject("event", null);
				Event.loadFrom(eventNode);
			}
		}
	}

	public static InterfaceImplementer getPublisherFactoryImplementer() {
		return MQHolder.Event.publisherFactoryImplementer();
	}

	public static InterfaceImplementer getSubscriberFactoryImplementer() {
		return MQHolder.Event.subscriberFactoryImplementer();
	}

	public static String getSubscriberGroup() {
		return MQHolder.Event.subscriberGroup();
	}

}
