package apros.codeart.rabbitmq;

/**
 * 
 * 
 * {@code host} rabbitMQ的宿主地址，可以使用标准格式host：port（例如host =
 * myhost.com：15672）。如果省略端口号，则使用默认的AMQP端口（15672）。
 * 
 * {@code virtualHost} 虚拟主机名称
 * 
 */
public record Server(String host, String virtualHost) {

}
