package apros.codeart.ddd.message.internal;

import java.util.List;

import apros.codeart.ddd.message.MessageLogFactory;
import apros.codeart.dto.DTObject;

public final class MessageLog {
	private MessageLog() {
	}

	public static void write(String id, String name, DTObject content) {
		var logger = MessageLogFactory.createLog();
		logger.write(id, name, content);
	}

	public static void flush(String id) {
		var logger = MessageLogFactory.createLog();
		logger.flush(id);
	}

	public static MessageEntry find(String id) {
		var logger = MessageLogFactory.createLog();
		return logger.find(id);
	}

	/**
	 * 
	 * 找到由于中断的原因要发送消息的编号
	 * 
	 * @return
	 */
	public static List<String> findInterrupteds() {
		var logger = MessageLogFactory.createLog();
		return logger.findInterrupteds();
	}

	/**
	 * 清理废弃的资源
	 */
	public static void cleanup() {
		var logger = MessageLogFactory.createLog();
		logger.cleanup();
	}

}
