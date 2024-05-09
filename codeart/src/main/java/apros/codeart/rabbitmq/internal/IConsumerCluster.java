package apros.codeart.rabbitmq.internal;

import java.time.Duration;

public interface IConsumerCluster {
	void tryScale();

	void messagesProcessed(Duration elapsed);
}
