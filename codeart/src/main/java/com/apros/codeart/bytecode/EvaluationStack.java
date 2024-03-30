package com.apros.codeart.bytecode;

import static com.apros.codeart.i18n.Language.strings;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 在Java中，方法的嵌套调用是通过调用栈（call stack）来实现的。每当你调用一个方法，Java虚拟机会在调用栈上创建一个新的栈帧（stack
 * frame），用于存储该方法的局部变量、参数和执行状态等信息。当方法执行完毕时，对应的栈帧会被从调用栈中移除，控制权回到上一个调用的方法上。
 * 
 * 因此，虽然方法之间的调用是通过共享调用栈来实现的，但是每个方法调用都会创建自己的栈帧，这些栈帧在调用栈中以堆栈的形式依次排列。每个栈帧都包含了方法的局部变量和其他执行上下文信息，因此它们之间是相互独立的，互不干扰。
 */
class EvaluationStack {
	private LinkedList<StackFrame> _frames;

	private StackFrame _current;

	public StackFrame currentFrame() {
		return _current;
	}

	public EvaluationStack() {
		_frames = new LinkedList<StackFrame>();
		this.enterFrame();
	}

	/**
	 * 进入栈帧
	 */
	public void enterFrame() {
		StackFrame frame = new StackFrame();
		_frames.push(frame);
		_current = frame;
	}

	/**
	 * 离开栈帧
	 */
	public void exitFrame() {
		_frames.pop();
		_current = _frames.peek();
	}

	public int frameSize() {
		return _frames.size();
	}

	/**
	 * 释放调用栈
	 */
	public void clearFrames() {
		_frames.clear();
		_current = null;
	}

	public void push(Class<?> type) {
		_current.push(new StackItem(type));
	}

	public void push(StackItem item) {
		_current.push(item);
	}

	public StackItem pop() {
		return _current.pop();
	}

	public StackItem peek() {
		return _current.peek();
	}

	public void pop(int count) {
		_current.pop(count);
	}

	public int size() {
		return _current.size();
	}

	/**
	 * 查找栈顶上得元素，得到他们得类型，如果他们之间得类型不相同，报错
	 * 
	 * @param expectCount 期望栈顶有几个值
	 * @return
	 */
	Class<?> matchType(int expectedCount) {
		return _current.matchType(expectedCount);
	}

	static class StackFrame {

		private LinkedList<StackItem> _items;

		public StackFrame() {
			_items = new LinkedList<StackItem>();
		}

		public List<StackItem> getItems() {
			return Collections.unmodifiableList(_items);
		}

//		public StackItem[] getItems(int expectedCount) {
//			var ts = new StackItem[expectedCount];
//			var i = expectedCount - 1;
//			for (var vt : _items) {
//				ts[i] = vt;
//				i--;
//			}
//			return ts;
//		}

		public void push(StackItem item) {
			_items.push(item);
		}

		public StackItem pop() {
			return _items.pop();
		}

		public StackItem peek() {
			return _items.peek();
		}

		public void pop(int count) {
			int pointer = 0;
			while (pointer < count) {
				_items.pop();
				pointer++;
			}
		}

		public int size() {
			return _items.size();
		}

		public boolean assertRefs(int expectedCount) {

			assertCount(expectedCount);

			for (StackItem item : _items) {
				if (item.isPrimitive())
					throw new IllegalArgumentException(strings("TypeMismatch"));
			}
			return true;
		}

		/**
		 * 检查栈顶值有多少个
		 * 
		 * @param count
		 */
		public void assertCount(int expectedCount) {
			if (this.size() != expectedCount)
				throw new IllegalArgumentException(strings("TypeMismatch"));
		}

		/**
		 * 查找栈顶上得元素，得到他们得类型，如果他们之间得类型不相同，报错
		 * 
		 * @param expectCount 期望栈顶有几个值
		 * @return
		 */
		Class<?> matchType(int expectedCount) {
			assertCount(expectedCount);
			Class<?> targetType = _items.get(0).getValueType();
			for (StackItem item : _items) {
				if (targetType != item.getValueType())
					throw new IllegalStateException(strings("TypeMismatch"));
			}
			return targetType;
		}

	}

}
