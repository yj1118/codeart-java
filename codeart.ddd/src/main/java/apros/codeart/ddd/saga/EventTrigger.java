package apros.codeart.ddd.saga;

import static apros.codeart.i18n.Language.strings;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.concurrent.BusySignal;

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
             var event = queue.next();
             if (event != null)
             {
            	 String eventName = event.name();
                 EventLog.flushRaise(queue, eventName); //一定要确保日志先被正确的写入，否则会有BUG
            	 args = queue.getArgs(eventName, args);
                 if (event.local() != null)
                 {
                	 // 本地事件，直接执行
                     args = raiseLocalEvent(event.local(), args, queue);
                 }
                 else
                 {
                	 args = raiseRemoteEvent(eventName, args, queue);
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

	private static DTObject raiseRemoteEvent(String eventName, DTObject args, EventQueue queue) {

		var eventId = queue.getEventId(eventName);
		// 先订阅触发事件的返回结果的事件
		subscribeRemoteEventResult(eventId);

		// 再发布“触发事件”的事件
		var raiseEventName = EventUtil.getRaise(eventName);

		var remoteArg = queue.getEntryRemotable(eventName, eventId, args);
		EventPortal.publish(raiseEventName, remoteArg); // 触发远程事件就是发布一个“触发事件”的事件
		// ，订阅者会收到消息后会执行触发操作

		BusySignal<DTObject> signal = createSignal(eventId);

		try {
			var output = signal.wait(10, TimeUnit.SECONDS);
			
			var error = output.getString("error",null);

			if (error != null)
			{
			    var ui = @event.GetValue<bool>("ui"); //ui错误
			    //如果没有执行成功，那么抛出异常
			    if(ui) throw new RemoteBusinessFailedException(message);
			    throw new RemoteEventFailedException(message);
			}
			
			var message = output.getString("message",null);
			
			if (message != null)
			{
				throw new RemoteBusinessFailedException(message);
			}
			

			var data = output.getObject("data");


			var entry = queue.GetEntry(key.EventId);
			if (entry.IsEmpty())
			{
			    throw new DomainEventException(string.Format(Strings.EventEntryNotExistWithCallbackTip, queue.Id, entry.EventId));
			}

			

			//远程事件执行完毕后，用它所在的源事件接受结果
			var source = queue.source();
			source.apply(eventName, data);
			
			return data;

		} catch (Exception ex) {
			throw ex;
		} finally {
			removeSignal(eventId);
			cleanupRemoteEventResult(eventId);
		}

	}

	private static void subscribeRemoteEventResult(String eventId) {
		var raiseResultEventName = EventUtil.getRaiseResult(eventId);
		EventPortal.subscribe(raiseResultEventName, ReceiveResultEventHandler.instance, true);
	}

	private static ConcurrentHashMap<String, BusySignal<DTObject>> _signals = new ConcurrentHashMap<String, BusySignal<DTObject>>();

	private static BusySignal<DTObject> createSignal(String id) {
		BusySignal<DTObject> signal = new BusySignal<>();
		_signals.put(id, signal);
		return signal;
	}

	private static void removeSignal(String id) {
		_signals.remove(id);
	}

	public static BusySignal<DTObject> getSignal(String id) {
		return _signals.get(id);
	}

}
