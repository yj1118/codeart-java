package com.apros.codeart.bytecode;

import static com.apros.codeart.i18n.Language.strings;

import java.util.LinkedList;

/**
 * 在Java中，方法的嵌套调用是通过调用栈（call stack）来实现的。每当你调用一个方法，Java虚拟机会在调用栈上创建一个新的栈帧（stack
 * frame），用于存储该方法的局部变量、参数和执行状态等信息。当方法执行完毕时，对应的栈帧会被从调用栈中移除，控制权回到上一个调用的方法上。
 * 
 * 因此，虽然方法之间的调用是通过共享调用栈来实现的，但是每个方法调用都会创建自己的栈帧，这些栈帧在调用栈中以堆栈的形式依次排列。每个栈帧都包含了方法的局部变量和其他执行上下文信息，因此它们之间是相互独立的，互不干扰。
 */
final class StackFrame {

	private LinkedList<StackItem> _items;

	public StackItem[] getItems(int expectedCount) {
		var ts = new StackItem[expectedCount];
		var i = expectedCount - 1;
		for (var vt : _items) {
			ts[i] = vt;
			i--;
		}
		return ts;
	}

//	public Class<?>[] getItemTypes(int expectedCount) {
//		var ts = new Class<?>[expectedCount];
//		var i = expectedCount - 1;
//		for (var vt : _items) {
//			ts[i] = vt.getType();
//			i--;
//		}
//		return ts;
//	}

	public void validateRefs(int expectedCount) {
		var items = this.getItems(expectedCount);
		for (var item : items) {
			if (item.isPrimitive())
				throw new IllegalArgumentException(strings("TypeMismatch"));
		}
	}

	public StackFrame() {
		_items = new LinkedList<StackItem>();
	}

	public void push(Class<?> type) {
		_items.push(new StackItem(type));
	}

	public void push(StackItem item) {
		_items.push(item);
	}

	public StackItem pop() {
		return _items.pop();
	}

	public int size() {
		return _items.size();
	}

	/**
	 * 检查栈顶至少值有多少个
	 * 
	 * @param count
	 */
	void validateLeast(int count) {
		if (size() < count) {
			throw new IllegalStateException(strings("TypeMismatch"));
		}
	}

	/**
	 * 查找栈顶上得元素，得到他们得类型，如果他们之间得类型不相同，报错
	 * 
	 * @param expectCount 期望栈顶有几个值
	 * @return
	 */
	Class<?> matchType(int expectedCount) {
		validateLeast(expectedCount);
		var items = this.getItems(expectedCount);
		var targetType = items[0].getValueType();
		for (var i = 1; i < items.length; i++) {
			if (targetType != items[i].getValueType())
				throw new IllegalStateException(strings("TypeMismatch"));
		}
		return targetType;
	}

}
