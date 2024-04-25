package apros.codeart.ddd.saga;

import java.util.UUID;

import apros.codeart.ddd.repository.DataContext;
import apros.codeart.dto.DTObject;

public final class EventTrigger {

	private EventTrigger() {
	}

	private static void raise(EventQueue queue,DTObject input, EventCallback callback)
	 {
	     boolean successful = false;
	     boolean running = false;
	     
	     DTObject args =  input;

	     while (!running && !successful)
	     {
	    	//触发队列事件
             var eventName = queue.next();
             if (eventName != null)
             {
                 EventLog.flushRaise(queue, eventName); //一定要确保日志先被正确的写入，否则会有BUG
                 if (queue.isLocal(eventName))
                 {
                	 // 本地事件，直接执行
                	 // 本地事件就是事件源，切领域事件仅支持1个本地事件和多个外部事件的联合调用
                	 // 不支持多个本地之间和多个外部事件的联合调用，主要是为了节省开销，而且实际上也没必要
                	 args = queue.getArgs(eventName, args);
                     var source = queue.source();

                     raiseLocalEvent(source, args, queue);
                 }
                 else
                 {

                     raiseRemoteEvent(eventName, args, queue);
                 }
             }

             EventQueue.Update(queue); 
             isRunning = queue.IsRunning;
             isSucceeded = queue.IsSucceeded;
	     }

	     bool completed = false;
	     DomainEvent @event = null;
	     if (isSucceeded)
	     {
	         if (queue.IsSubqueue)
	         {
	             var entry = queue.Source;
	             var argsCode = entry.ArgsCode;

	             var identity = queue.GetIdentity();
	             var eventName = entry.EventName;
	             var eventId = entry.EventId;
	             callback.Mount(() => //挂载回调事件，这样所有操作执行完毕后，会发布事件被完成的消息
	             {
	                 //发布事件被完成的消息
	                 PublishRaiseSucceeded(identity, eventName, eventId, argsCode);
	             });
	         }
	         else
	         {
	             //不是被外界调用的，所以整个事件场景已完成
	             completed = true;
	             @event = queue.Source.GetSourceEvent();
	         }

	         EventLog.FlushEnd(queue.Id); //指示恢复管理器事件队列的操作已经全部完成
	     }

	     if (completed)
	     {
	         callback.Mount(() -> //挂载回调事件
	         {
	             DomainEvent.OnSucceeded(queue.Id, @event);
	         });
	     }
	 }

	private static DTObject raiseLocalEvent(DomainEvent event, DTObject args, EventQueue queue) {
		var eventName = event.name();
		return DataContext.newScope(() -> {
			var ctx = queue.createContext(eventName);
			var output = event.raise(args, ctx);
			// 当本地事件执行完毕后，执行它所在的源事件的PreEventCompleted方法
			event.apply(eventName, output);
			// 这里上下文如果保存失败了，那么事务肯定也执行失败，没问题
			// 上下文如果保存成功了，那么当commit提交失败了，由于幂等性，就算执行回溯了，也没事
			ctx.save();
			return output;
		});
	}

	private static void raiseRemoteEvent(String eventName, DTObject args, EventQueue queue) {

		var eventId = queue.getEventId(eventName);
		// 先订阅触发事件的返回结果的事件
		subscribeRemoteEventResult(eventName, eventId);

		// 再发布“触发事件”的事件
		var raiseEventName = EventUtil.GetRaise(entry.EventName);
		EventPortal.Publish(raiseEventName, entry.GetRemotable(identity, args)); // 触发远程事件就是发布一个“触发事件”的事件
																					// ，订阅者会收到消息后会执行触发操作

		TimeoutManager.Start(eventKey);
	}

	private static void subscribeRemoteEventResult(String eventName, String eventId) {
		var raiseResultEventName = EventUtil.getRaiseResult(eventName, eventId);
		EventPortal.Subscribe(raiseResultEventName, ReceiveResultEventHandler.Instance, true);
	}

}
