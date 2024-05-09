package apros.codeart.rabbitmq;

public interface IConsumer {

	void open();

	void close();

	boolean disposed();

}
