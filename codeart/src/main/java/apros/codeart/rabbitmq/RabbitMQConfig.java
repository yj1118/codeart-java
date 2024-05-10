package apros.codeart.rabbitmq;

import apros.codeart.AppConfig;
import apros.codeart.util.ListUtil;

public final class RabbitMQConfig {
	private RabbitMQConfig() {

	}

	public static MQConnConfig find(String name) {
		var mq = AppConfig.section("rabbitMQ");
		if (mq == null)
			return null;

		var ps = mq.getObjects("servers", false);
		if (ps == null)
			return null;

		var t = ListUtil.find(ps, (p) -> p.getString("name").equalsIgnoreCase(name));
		if (t == null)
			return null;

		var host = t.getString("host");
		var vhost = t.getString("vhost");
		var uid = t.getString("uid");
		var pwd = t.getString("pwd");

		return new MQConnConfig(host, vhost, uid, pwd);

	}

}
