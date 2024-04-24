package apros.codeart.ddd.saga.internal;

import java.util.UUID;

import apros.codeart.ddd.saga.DomainEvent;

public final class EventTrigger {

	private EventTrigger() {
	}

	private static void raise(EventQueue queue, EventCallback callback)
	 {
	     boolean successful = false;
	     boolean running = false;

	     while (!running && !successful)
	     {
	    	//触发队列事件
             var entry = queue.next();
             if (!entry.isEmpty())
             {
                 var args = entry.GetArgs(); //获取条目的事件参数
                 EventLog.FlushRaise(queue, entry); //一定要确保日志先被正确的写入，否则会有BUG
                 if (entry.IsLocal)
                 {
                     var source = entry.GetSourceEvent();
                     var local = EventFactory.GetLocalEvent(entry, args, true);
                     RaiseLocalEvent(entry, local, source);
                 }
                 else
                 {
                     var identity = queue.GetIdentity();
                     RaiseRemoteEvent(entry, identity, args);
                 }
             }

             EventQueue.Update(queue); //这里队列的修改和业务的处理共享一个事务，因为业务的处理会影响队列的状态改变，两者必须原子性，要么一起成功，要么一起失败
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
	         callback.Mount(() => //挂载回调事件
	         {
	             DomainEvent.OnSucceeded(queue.Id, @event);
	         });
	     }
	 }

}
