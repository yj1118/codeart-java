package apros.codeart.echo.event;

import apros.codeart.dto.DTObject;
import apros.codeart.echo.EchoConfig;

public final class EchoEvent {
    private EchoEvent() {
    }

    private static class EchoEventHolder {

        private static final EventConfig Config;

        private static final DTObject Section;

        static {

            Config = new EventConfig();

            DTObject event = EchoConfig.eventSection();
            Config.loadFrom(event);
            Section = event;
        }
    }

    public static DTObject section() {
        return EchoEventHolder.Section;
    }

    public static String getSubscriberGroup() {
        return EchoEventHolder.Config.subscriberGroup();
    }

    private static class EventConfig {

        public EventConfig() {
            this._subscriberGroup = "default";
        }

        private String _subscriberGroup;

        /**
         * 订阅者分组，每个分组里的订阅者会均衡的处理收到的事件
         *
         * @return
         */
        public String subscriberGroup() {
            return _subscriberGroup;
        }

        public void loadFrom(DTObject root) {
            if (root == null)
                return;
            loadSubscriber(root);
        }

        private void loadSubscriber(DTObject root) {
            _subscriberGroup = root.getString("@subscriber.group", "default");
        }

    }

}
