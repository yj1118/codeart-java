package apros.codeart.rabbitmq;

import apros.codeart.AppConfig;
import apros.codeart.util.ListUtil;

final class RabbitMQConfig {
	private RabbitMQConfig() {

	}

	public static Policy find(String name, int prefetchCount, boolean publisherConfirms, boolean persistentMessages) {
		var mq = AppConfig.section("mq");
		if (mq == null)
			return null;

		var ps = mq.getObjects("rabbitmq", false);
		if (ps == null)
			return null;

		var t = ListUtil.find(ps, (p) -> p.getString("name").equalsIgnoreCase(name));
		if (t == null)
			return null;

		var host = t.getString("host");
		var vhost = t.getString("vhost");
		var uid = t.getString("uid");
		var pwd = t.getString("pwd");

		return new Policy(name, new Server(host, vhost), new User(uid, pwd), prefetchCount, publisherConfirms,
				persistentMessages);

	}

}
