package apros.codeart.mq.event;

import apros.codeart.dto.DTObject;

/**
 * 事件发布者
 */
public interface IPublisher {

	/**
	 * 发布远程事件
	 * 
	 * @param eventName
	 * @param event
	 */
	void publish(String eventName, DTObject event);
}
