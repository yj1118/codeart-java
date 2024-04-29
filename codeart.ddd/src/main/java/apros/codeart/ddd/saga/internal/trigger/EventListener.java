package apros.codeart.ddd.saga.internal.trigger;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.saga.internal.EventLoader;
import apros.codeart.ddd.saga.internal.EventUtil;
import apros.codeart.ddd.saga.internal.protector.EventProtector;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.StringUtil;

final class EventListener {
	private EventListener() {
	}

	/**
	 * 
	 * 接收到调用
	 * 
	 * @param e
	 */
	public static void accept(DTObject e) {
		var queueId = e.getString("id");
		var eventName = e.getString("eventName");
		var eventId = e.getString("eventId");
		var identity = e.getObject("identity");

		boolean throwError = false;
		try {

			AppSession.setIdentity(identity);

			var input = e.getObject("args");
			var source = EventLoader.find(eventName, true);

			var queue = new EventQueue(queueId, source, input);

			var args = EventTrigger.raise(queue);

			publishRaiseSuccess(args, eventName, eventId, identity);

		} catch (Exception ex) {

			throwError = true;
			// 发生了错误就发布出去，通知失败了
			publishRaiseFailed(eventName, eventId, identity, ex);

			// 恢复
		} finally {
			if (throwError && queueId != null)
				EventProtector.restore(queueId, false);
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
		var arg = createPublishRaiseResultArg(args, eventName, eventId, identity, null);
		EventPortal.publish(en, arg);
	}

	private static DTObject createPublishRaiseResultArg(DTObject args, String eventName, String eventId,
			DTObject identity, String error) {
		var output = DTObject.editable();

		output.setString("eventName", eventName);
		output.setString("eventId", eventId);

		if (!StringUtil.isNullOrEmpty(error)) {
			output.setString("error", error);
		}

		if (args != null) {
			output.setObject("args", args);
		}

		output.setObject("identity", identity);
		return output;
	}

	/**
	 * 
	 * 发布事件调用失败的结果
	 * 
	 * @param eventName
	 * @param eventId
	 * @param identity
	 * @param ex
	 */
	private static void publishRaiseFailed(String eventName, String eventId, DTObject identity, Exception ex) {
		var en = EventUtil.getRaiseResult(eventId);// 消息队列的事件名称

		var error = ex.getMessage();

		var arg = createPublishRaiseResultArg(null, eventName, eventId, identity, error);
		EventPortal.publish(en, arg);
	}

	/**
	 * 
	 * 收到调用结果
	 * 
	 * @param e
	 */
	public static void receive(DTObject e) {

		var eventId = e.getString("eventId");

		EventTrigger.continueRaise(eventId, e);

//	     var key = EventEntry.GetEventKey(@event);
//	     var queue = GetQueueInfo(key);
//	     EventProtector.UseExistedQueue(queue.Id, (callback) ->
//	      {
//	          EventTrigger.Continue(queue.Id, @event, key, callback);
//	      }, (ex) ->
//	      {
//	          if (queue.IsSubqueue)
//	          {
//	             //发生了错误就发布出去，通知失败了
//	             EventTrigger.PublishRaiseFailed(AppContext.Identity, key, ex); //再恢复
//	         }
//	          else
//	          {
//	             //如果不是外界调用而引起的事件，那么出现错误后只用恢复即可，不需要做额外的处理，内部会处理好
//	          }
//	      });
	}

}
