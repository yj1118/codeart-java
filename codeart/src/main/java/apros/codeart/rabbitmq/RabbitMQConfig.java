package apros.codeart.rabbitmq;

import apros.codeart.AppConfig;

import java.util.HashMap;

public final class RabbitMQConfig {
    private RabbitMQConfig() {

    }

    private static HashMap<String, String> getData(String input) {
        HashMap<String, String> data = new HashMap<>();
        String[] pairs = input.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0];
            String value = keyValue[1];
            data.put(key, value);
        }
        return data;
    }


    public static MQConnConfig find(String path) {
        var connString = AppConfig.getString(path);
        if (connString == null)
            return null;

        var data = getData(connString);
		
        var host = data.get("host");
        var vhost = data.get("vhost");
        var uid = data.get("uid");
        var pwd = data.get("pwd");

        return new MQConnConfig(host, vhost, uid, pwd);

    }

}
