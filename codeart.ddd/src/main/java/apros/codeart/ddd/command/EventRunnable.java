package apros.codeart.ddd.command;

import apros.codeart.ddd.saga.internal.EventLoader;
import apros.codeart.ddd.saga.internal.trigger.EventTrigger;
import apros.codeart.dto.DTObject;

public final class EventRunnable {

	private EventRunnable() {
	}

	public void execute(String eventName, DTObject input) {
		var event = EventLoader.find(eventName, true);
		EventTrigger.start(event, input);
	}

}
