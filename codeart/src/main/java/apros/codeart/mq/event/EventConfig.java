package apros.codeart.mq.event;

import apros.codeart.InterfaceImplementer;
import apros.codeart.dto.DTObject;

public class EventConfig {

	public EventConfig() {
		this._subscriberGroup = "default";
	}

	private InterfaceImplementer _publisherFactoryImplementer;

	/**
	 * 
	 * 事件发布者工厂的实现
	 * 
	 * @return
	 */
	public InterfaceImplementer publisherFactoryImplementer() {
		return _publisherFactoryImplementer;
	}

	private InterfaceImplementer _subscriberFactoryImplementer;

	/**
	 * 
	 * 事件订阅者工厂的实现
	 * 
	 * @return
	 */
	public InterfaceImplementer subscriberFactoryImplementer() {
		return _subscriberFactoryImplementer;
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
		loadPublisher(root);
		loadSubscriber(root);
	}

	private void loadPublisher(DTObject root) {
		var factory = root.getObject("publisher.factory", null);

		if (factory != null)
			_publisherFactoryImplementer = InterfaceImplementer.create(factory);
	}

	private void loadSubscriber(DTObject root) {
		var node = root.getObject("subscriber", null);
		if (node == null)
			return;

		_subscriberGroup = node.getString("group", "default");

		var factory = node.getObject("factory");
		if (factory != null)
			_subscriberFactoryImplementer = InterfaceImplementer.create(factory);
	}

}
