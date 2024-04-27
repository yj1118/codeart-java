package apros.codeart.ddd.saga;

import apros.codeart.dto.DTObject;

final class EventListener {
	private EventListener() {
	}

	public static void accept(DTObject input)
	 {
	     var eventId = input.get
	     try
	     {
	         var args = input.getObject("args");
	         var source = EventFactory.GetLocalEvent(key.EventName, args, true);
	         var queueId = key.EventId; //将外界的事件编号作为本地的队列编号
	         EventProtector.UseNewQueue(queueId, (callback) =>
	         {
	             EventTrigger.Start(key.EventId, source, true, callback); //我们把调用方指定的事件编号作为本地的事件队列编号
	         },
	         (ex) =>
	         {
	             //发生了错误就发布出去，通知失败了
	             EventTrigger.PublishRaiseFailed(AppContext.Identity, key, ex);
	         });
	     }
	     catch(Exception ex)
	     {
	         //发生了错误就发布出去
	         EventTrigger.PublishRaiseFailed(AppContext.Identity, key, ex);
	     }
	    
	 }

}
