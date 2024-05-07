package apros.codeart.rabbitmq;

import org.apache.logging.log4j.util.Strings;

public record User(String name, String password) {
	public final static User Empty = new User(Strings.EMPTY, Strings.EMPTY);
}
