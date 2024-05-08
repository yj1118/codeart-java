package apros.codeart.rabbitmq;

public interface IMessageHandler {

	/**
	 * 
	 * 请在处理完消息后调用应答方法
	 * 
	 * @param msg
	 */
	void handle(RabbitBus sender, Message message);
}
