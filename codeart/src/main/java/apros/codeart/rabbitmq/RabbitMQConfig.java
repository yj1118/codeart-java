package apros.codeart.rabbitmq;

import apros.codeart.AppConfig;

public final class RabbitMQConfig {
	private RabbitMQConfig() {

	}

	public static MQConnConfig find(String path) {
		var section = AppConfig.section(path);
		if (section == null)
			return null;

		var host = section.getString("host");
		var vhost = section.getString("vhost");
		var uid = section.getString("uid");
		var pwd = section.getString("pwd");

		return new MQConnConfig(host, vhost, uid, pwd);

	}

}
