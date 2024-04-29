package apros.codeart.ddd.saga.internal.protector;

import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.dto.DTObject;

record EventEntry(String name, String id, DomainEvent local, DTObject log) {

	public boolean isLocal() {
		return this.local != null;
	}

}
