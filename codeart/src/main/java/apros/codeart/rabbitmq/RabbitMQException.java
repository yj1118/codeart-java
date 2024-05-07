package apros.codeart.rabbitmq;

import apros.codeart.UIException;

/**
 * 对于rabbit的异常,大多数不需要追踪调用栈就可以分析问题，所以继承自UserUIException
 */
public class RabbitMQException extends UIException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5251318871762067015L;

	public RabbitMQException() {
		super();
	}

	public RabbitMQException(String message) {
		super(message);
	}
}
