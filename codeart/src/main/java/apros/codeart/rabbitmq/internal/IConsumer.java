package apros.codeart.rabbitmq.internal;

public interface IConsumer {

	void open();

	void close();

	boolean disposed();

}
