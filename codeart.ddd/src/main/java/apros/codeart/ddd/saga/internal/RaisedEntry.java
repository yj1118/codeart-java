package apros.codeart.ddd.saga.internal;

import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.dto.DTObject;

public record RaisedEntry(String name, String id, DomainEvent local, DTObject log) {

	public boolean isLocal() {
		return this.local != null;
	}

}
