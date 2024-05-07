package apros.codeart.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.StringUtil;

public final class Policy {

	private String _name;

	/**
	 * 
	 * 策略名称
	 * 
	 * @return
	 */
	public String name() {
		return _name;
	}

	private Server _server;

	/**
	 * 
	 * 该策略用到的RabbitMQ服务相关的信息
	 * 
	 * @return
	 */
	public Server server() {
		return _server;
	}

	private User _user;

	public User user() {
		return _user;
	}

	private int _prefetchCount;

	/**
	 * 
	 * 消费者在开启acknowledge的情况下，对接收到的消息可以根据业务的需要异步对消息进行确认。
	 * 然而在实际使用过程中，由于消费者自身处理能力有限，从rabbitMQ获取一定数量的消息后，
	 * 希望rabbitmq不再将队列中的消息推送过来，当对消息处理完后（即对消息进行了ack，并且有能力处理更多的消息）
	 * 再接收来自队列的消息。在这种场景下，我们可以通过设置PrefetchCount来达到这种效果。
	 * 如果PrefetchCount设置为50，表示最多同时处理50个消息，多余的消息RabbitMQ会堆积在服务器或者给其他的消费者处理
	 * 
	 * @return
	 */
	public int prefetchCount() {
		return _prefetchCount;
	}

	private boolean _publisherConfirms;

	/**
	 * 
	 * 发送方确认模式，一旦信道进入该模式，所有在信道上发布的消息都会被指派以个唯一的ID号（从1开始）。
	 * 一旦消息被成功投递给所匹配的队列后，信道会发送一个确认给生产者（包含消息的唯一ID）。这使得生产者
	 * 知晓消息已经安全达到目的队列了。如果消息和队列是可持久化的，那么确认消息只会在队列将消息写入磁盘后才会发出。
	 * 如果RabbitMQ服务器发生了宕机或者内部错误导致了消息的丢失，Rabbit会发送一条nack消息给生产者，表明消息已经丢失。
	 * 该模式性能优秀可以取代性能低下的发送消息事务机制。 在需要严谨的、消息必须送达的情况下需要开启该模式。
	 * 
	 * 
	 * 注意，发布者确认模式，何时确认是rabbitMQ服务器的处理，跟程序员无关，程序员不需要手工去确认。这跟消息确认模式不同。
	 * 
	 * @return
	 */
	public boolean publisherConfirms() {
		return _publisherConfirms;
	}

	private boolean _persistentMessages;

	/**
	 * 
	 * 是否持久化消息，如果为true，消息会被写入到日志，只有当消费者确认消费了该消息后，才会从日志中清除
	 * 持久化的消息是可以恢复的，就算服务器宕机了也可以保证消息依然能正确的送达 所以在需要严谨的、消息必须送达的情况下需要开启该模式。
	 * 
	 * @return
	 */
	public boolean persistentMessages() {
		return _persistentMessages;
	}

	private String _connectionString;

	public String connectionString() {
		return _connectionString;
	}

	public Policy(String name, Server server, User user, int prefetchCount, boolean publisherConfirms,
			boolean persistentMessages) {
		_name = name;
		_server = server;
		_user = user;
		_prefetchCount = prefetchCount;
		_publisherConfirms = publisherConfirms;
		_persistentMessages = persistentMessages;
		_connectionString = getConnectionString();
	}

	private String getConnectionString() {
		StringBuilder code = new StringBuilder();
		StringUtil.appendFormat(code, "host=%s;", this.server().host());
		StringUtil.appendFormat(code, "virtualHost=%s;", this.server().virtualHost());
		StringUtil.appendFormat(code, "username=%s;", this.user().name());
		StringUtil.appendFormat(code, "password=%s;", this.user().password());
		StringUtil.appendFormat(code, "prefetchcount=%s;", this.prefetchCount());

		if (this.publisherConfirms()) {
			code.append("publisherConfirms=true;");
		} else
			code.append("publisherConfirms=false;");

		if (this.persistentMessages()) {
			code.append("persistentMessages=true;");
		} else
			code.append("persistentMessages=false;");

		return code.toString();
	}

	@Override
	public int hashCode() {
		return _connectionString.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		var target = TypeUtil.as(obj, Policy.class);
		if (target == null)
			return false;
		return target.connectionString().equals(this.connectionString());
	}

	void init(ConnectionFactory factory) {
		factory.setHost(this.server().host());
		factory.setVirtualHost(this.server().virtualHost());
		factory.setUsername(this.user().name());
		factory.setPassword(this.user().password());
	}
}
