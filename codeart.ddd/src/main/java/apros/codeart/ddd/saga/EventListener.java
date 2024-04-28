package apros.codeart.ddd.saga;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.saga.internal.EventLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.StringUtil;

final class EventListener {
	private EventListener() {
	}

	public static void accept(DTObject e) {
		var eventName = e.getString("eventName");
		var eventId = e.getString("eventId");
		var identity = e.getObject("identity");
		try {
			AppSession.setIdentity(identity);

			var input = e.getObject("args");
			var source = EventLoader.find(eventName, true);

			var args = EventTrigger.start(source, input);

			publishRaiseSuccess(args, eventName, eventId, identity);

		} catch (Exception ex) {
			// 发生了错误就发布出去，通知失败了
			publishRaiseFailed(eventName, eventId, identity, ex);

			// 恢复
		}

	}

	/**
	 * 
	 * 发布事件调用成功的结果
	 * 
	 * @param args
	 * @param ctx
	 */
	private static void publishRaiseSuccess(DTObject args, String eventName, String eventId, DTObject identity) {
		var en = EventUtil.getRaiseResult(eventId); // 消息队列的事件名称
		// 返回事件成功被执行的结果
		var arg = createPublishRaiseResultArg(args, eventName, eventId, identity, null, false);
		EventPortal.publish(en, arg);
	}

	private static DTObject createPublishRaiseResultArg(DTObject args, String eventName, String eventId,
			DTObject identity, String error, boolean isBusinessException) {
		var output = DTObject.editable();

		output.setString("eventName", eventName);
		output.setString("eventId", eventId);

		if (!StringUtil.isNullOrEmpty(error)) {
			if (isBusinessException)
				output.setString("message", error);
			else
				output.setString("error", error);
		}

		if (args != null) {
			output.setObject("args", args);
		}

		output.setObject("identity", identity);
		return output;
	}

	/// <summary>
	/// 发布事件调用失败的结果
	/// </summary>

	static void publishRaiseFailed(String eventName, String eventId, DTObject identity, Exception ex) {
		var en = EventUtil.getRaiseResult(eventId);// 消息队列的事件名称

		var error = ex.GetCompleteInfo();

		var isBusinessException = ex.IsUserUIException();

		var arg = createPublishRaiseResultArg(null, eventName, eventId, identity, error, isBusinessException);
		EventPortal.publish(en, arg);

	}

}
