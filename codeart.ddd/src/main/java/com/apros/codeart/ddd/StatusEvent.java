package com.apros.codeart.ddd;

import java.util.function.Function;

import com.apros.codeart.context.AppSession;
import com.apros.codeart.util.EventHandler;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.MapList;
import com.apros.codeart.util.ReaderWriterLockSlim;

/**
 * 状态事件，与某一个领域对象的状态相关的事件
 */
public final class StatusEvent {

	private StatusEvent() {
	}

	/**
	 * 仅针对类型 {@code objectType} 注册状态事件
	 * 
	 * @param <T>
	 * @param objectType
	 * @param eventType
	 * @param handler
	 */
	public static <T extends IDomainObject> void register(Class<T> objectType, StatusEventType eventType,
			EventHandler<StatusEventArgs> handler) {
		register(objectType.getSimpleName(), eventType, handler);
	}

	public static void register(String objectTypeName, StatusEventType eventType,
			EventHandler<StatusEventArgs> handler) {
		String eventName = getEventName(objectTypeName, eventType);
		registerEvent(eventName, handler);
	}

	/// <summary>
	/// 注册全局状态事件，任何对象的状态改变都会触发该事件
	/// </summary>
	/// <param name="eventType"></param>
	/// <param name="action"></param>
	public static void register(StatusEventType eventType, EventHandler<StatusEventArgs> handler) {
		String eventName = getEventName(IDomainObject.class.getSimpleName(), eventType);
		registerEvent(eventName, handler);
	}

//	region 执行状态事件

	public static void execute(StatusEventType eventType, IDomainObject obj) {

		var objectType = obj.getClass();
		{
			// 先触发类型级别的事件
			var args = getEventArgs(objectType);
			var eventName = getEventName(objectType.getSimpleName(), eventType);
			dispatchEvent(eventName, obj, args);
		}

		{
			// 触发全局事件
			var args = getEventArgs(IDomainObject.class);
			var eventName = getEventName(IDomainObject.class.getSimpleName(), eventType);
			dispatchEvent(eventName, obj, args);
		}
	}

//	#endregion

//	region 获取状态事件的参数数据

	/// <summary>
	/// 获取事件参数
	/// </summary>
	/// <param name="objectType"></param>
	/// <param name="eventType"></param>
	/// <returns></returns>
	public static StatusEventArgs getEventArgs(Class<? extends IDomainObject> objectType) {
		String eventName = getEventName(objectType.getSimpleName(), StatusEventType.Any); // 通一会话中，同一个对象类型的状态事件的参数只有一个，不需要通过事件类型再来划分，因为这样用起来不灵活
		String argsName = _getStatusEventArgsName.apply(eventName);
		return AppSession.obtainItem(argsName, () -> {
			return new StatusEventArgs();
		});
	}

//	#endregion

	private static String getEventName(String objectTypeName, StatusEventType eventType) {
		return _getStatusEventName.apply(objectTypeName).apply(eventType);
	}

	private static Function<String, Function<StatusEventType, String>> _getStatusEventName = LazyIndexer
			.init((objectTypeName) -> {
				return LazyIndexer.init((eventType) -> {
					return String.format("StatusEvent_%s_%s", objectTypeName, eventType.getValue());
				});
			});

	private static Function<String, String> _getStatusEventArgsName = LazyIndexer.init((eventName) -> {
		return String.format("{0}_args", eventName);
	});

//	region 数据

	private static void dispatchEvent(String eventName, Object sender, StatusEventArgs arg) {
		var events = _eventLock.readGet(() -> {
			return _events.get(eventName);
		});

		if (events != null) {
			for (var evt : events) {
				evt.raise(sender, () -> arg);
			}
		}
	}

	private static void registerEvent(String eventName, EventHandler<StatusEventArgs> handler) {
		_eventLock.writeRun(() -> {
			_events.put(eventName, handler);
		});
	}

	private static MapList<String, EventHandler<StatusEventArgs>> _events = new MapList<String, EventHandler<StatusEventArgs>>(
			false);

	private static final ReaderWriterLockSlim _eventLock = new ReaderWriterLockSlim();

//	#endregion

}
