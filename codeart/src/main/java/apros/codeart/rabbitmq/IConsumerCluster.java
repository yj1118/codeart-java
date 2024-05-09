package apros.codeart.rabbitmq;

import java.time.Duration;

public interface IConsumerCluster {
	void tryScale();

	void messagesProcessed(Duration elapsed);
}
