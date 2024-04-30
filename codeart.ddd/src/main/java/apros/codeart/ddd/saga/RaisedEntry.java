package apros.codeart.ddd.saga;

import apros.codeart.ddd.saga.internal.EventLoader;
import apros.codeart.dto.DTObject;

public record RaisedEntry(int index, String name, DTObject log) {

	public DomainEvent local() {
		return EventLoader.find(this.name(), false);
	}

}
