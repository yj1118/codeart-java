//package com.apros.codeart.bytecode;
//
//import java.util.ArrayList;
//
//class CodeScope implements AutoCloseable {
//	private final MethodGenerator _owner;
//	private final ArrayList<BorrowedLocal> _ownedLocals = new ArrayList<BorrowedLocal>();
//
//	/// <summary>
//	/// 代码段开始时，计算堆栈的深度
//	/// </summary>
//	private int _startEvalStackDepth;
//
//	/**
//	 * 该字段表示，在代码段结束后，相对于刚进入代码段的时候，多出的堆栈空间，
//	 * 
//	 * 比如说，在该代码段里，执行了语句加载一个int数据，这时候代码段结束，那么堆栈就多出了1的空间
//	 */
//	private int _evalStackOverflow;
//	private boolean _closed;
//
//	public CodeScope(MethodGenerator owner) {
//		_owner = owner;
//	}
//
//	public CodeScope(MethodGenerator owner, int evalStackOverflow) {
//		this(owner);
//		_evalStackOverflow = evalStackOverflow;
//	}
//
//	MethodGenerator getOwner() {
//		return _owner;
//	}
//
//	public IVariable declareLocal(Class<?> type) {
//		return declareLocal(type, null);
//	}
//
//	public IVariable declareLocal(Class<?> type, String name) {
//		var result = _owner.borrowLocal(type, name);
//		_ownedLocals.add(result);
//		return result;
//	}
//
//	public virtual void Open()
//	{
//	    _startEvalStackDepth = Owner._evalStack.Count;
//	}
//
//	/// <summary>
//	/// 结束代码段
//	/// </summary>
//	protected virtual void Close()
//	{
//	    if (_closed) return;
//
//	    try
//	    {
//	        //释放该代码段的局部变量
//	        foreach (var local in _ownedLocals)
//	        {
//	            local.Dispose();
//	        }
//
//	        if (Owner._evalStack.Count != (_startEvalStackDepth + _evalStackOverflow))
//	        {
//	            throw new InvalidOperationException(string.Format("Begin/End 代码段验证失败 - 开始代码段时，有 {0} 项在计算堆栈，但是结束时，还有 {1} 项，预期应该有 {2} 项",
//	                                                                                    _startEvalStackDepth, Owner._evalStack.Count, _startEvalStackDepth + _evalStackOverflow));
//	        }
//	    }
//	    finally
//	    {
//	        _closed = true;
//	    }
//	}
//
//	public void Dispose() {
//		Close();
//	}
//}
