package apros.codeart.ddd.message;

import apros.codeart.ddd.message.internal.MessageEntry;
import apros.codeart.dto.DTObject;

public interface IMessageLog {
	void write(String id, String name, DTObject content);

	void flush(String id);

	MessageEntry find(String id);

	void cleanup();
}