package apros.codeart.ddd.launcher;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.cqrs.internal.Forker;
import apros.codeart.ddd.cqrs.internal.RemoteService;
import apros.codeart.ddd.message.internal.MessageHost;
import apros.codeart.ddd.metadata.MetadataLoader;
import apros.codeart.ddd.remotable.internal.RemotableImpl;
import apros.codeart.ddd.repository.access.DataModelLoader;
import apros.codeart.ddd.saga.internal.EventHost;
import apros.codeart.i18n.Language;

final class DomainHost {

	private DomainHost() {

	}

	private static boolean _initialized = false;

//
//	/// <summary>
//	/// <para>初始化与领域对象相关的行为，由于许多行为是第一次使用对象的时候才触发，这样会导致没有使用对象却需要一些额外特性的时候缺少数据</para>
//	/// <para>所以我们需要在使用领域驱动的之前执行初始化操作</para>
//	/// </summary>
//	internal
//
	public static void initialize() {
		if (_initialized)
			return;
		_initialized = true;

		// 以下代码执行顺序不能变

		var domainTypes = MetadataLoader.load();

		DataModelLoader.load(domainTypes);

		// 执行远程能力特性的初始化，收集相关数据
		RemotableImpl.initialize();

		// 远程服务的初始化
		RemoteService.initialize();

		// 领域事件宿主的初始化
		EventHost.initialize();

		// 领域消息的初始化
		MessageHost.initialize();

		// 复刻数据初始化
		Forker.initialize();
	}

	/**
	 * 初始化之后
	 */
	public static void initialized() {
		EventHost.initialized();
		MessageHost.initialized();
	}

	public static void cleanup() {
		RemoteService.cleanup();
		EventHost.cleanup();
	}

	/**
	 * 检查领域对象是否已被初始化了
	 */
	static void checkInitialized() {
		if (!_initialized) {
			throw new DomainDrivenException(Language.strings("codeart.ddd", "UninitializedDomainObject"));
		}
	}

}
