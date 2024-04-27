package apros.codeart.ddd.saga;

/**
 * {@name 条目名称，也就是对应的事件名称}
 * 
 * {@local 条目对应的本地事件，如果为null则为远程事件}
 * 
 * {@expanded 是否在队列中已将整个事件展开，达到可以执行的准备状态}
 * 
 * {@source 事件来源，即：是由哪个DomainEvent定义的}
 */
record EventEntry(String name, DomainEvent local, boolean expanded, DomainEvent source) {

	public boolean isLocal() {
		return this.local != null;
	}

}
