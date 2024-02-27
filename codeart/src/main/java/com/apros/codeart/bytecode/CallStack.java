package com.apros.codeart.bytecode;

import java.util.LinkedList;

/**
 * 调用栈
 */
class CallStack {
	private LinkedList<StackFrame> _frames;

	private StackFrame _current;

	public StackFrame frame() {
		return _current;
	}

	public CallStack() {
		_frames = new LinkedList<StackFrame>();
	}

	public void push() {
		var frame = new StackFrame();
		_frames.push(frame);
		_current = frame;
	}

	public void pop() {
		_frames.pop();
		_current = _frames.peek();
	}

	public void clear() {
		_frames.clear();
		_current = null;
	}

}
