package com.apros.codeart.bytecode;

import static com.apros.codeart.i18n.Language.strings;

import java.util.ArrayList;
import java.util.LinkedList;

import org.objectweb.asm.Label;

final class ScopeStack {

	private LinkedList<CodeScope> _scopes;

	private CodeScope _current;

	private MethodGenerator _owner;

	public ScopeStack(MethodGenerator owner, Iterable<MethodParameter> prms) {
		_scopes = new LinkedList<CodeScope>();
		_owner = owner;
		this.init(prms);
	}

	public Label getStartLabel() {
		return _current.getStartLabel();
	}

	public Label getEndLabel() {
		return _current.getEndLabel();
	}

	private void init(Iterable<MethodParameter> prms) {
		this.enter();
		// 将主体方法的变量加入到根范围中
		for (var prm : prms) {
			this.declare(prm.getType(), prm.getName());
		}
	}

	public void enter() {
		var scope = _enter();
		_owner.visitor().visitLabel(scope.getStartLabel());
	}

	/**
	 * 用自定义的开始标签进入代码范围，这表示代码范围不会自动标记开始位置
	 * 
	 * @param startLabel
	 */
	public void enter(Label startLabel) {
		var scope = _enter();
		scope.setStartLabel(startLabel);
		_owner.visitor().visitLabel(scope.getStartLabel());
	}

	private CodeScope _enter() {
		StackAssert.assertClean(_owner, () -> {
			return strings("codeart", "EnterScopeStackNotEmpty");
		});

		var scope = new CodeScope(_owner);
		_scopes.push(scope);
		_current = scope;
		return scope;
	}

	public int getDepth() {
		return _scopes.size();
	}

	public void exit() {
		StackAssert.assertClean(_owner, () -> {
			return strings("codeart", "ExitScopeStackNotEmpty");
		});

		_owner.visitor().visitLabel(_current.getEndLabel());

		var scope = _scopes.pop();
		scope.close();
		_current = _scopes.peek();
	}

	public void using(Runnable action) {
		this.enter();
		action.run();
		this.exit();
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public Label createLabel() {
		var label = new Label();
		_owner.visitor().visitLabel(label);
		return label;
	}

//	/// <summary>
//	/// 将新的代码范围压入范围堆栈
//	/// </summary>
//	/// <param name="newScope"></param>
//	private void PushScope(Scope newScope) {
//		_scopeStack.Push(_currentScope);
//		_currentScope = newScope;
//		newScope.Open();
//	}
//
//	/// <summary>
//	///
//	/// </summary>
//	/// <param name="expectedScopeType">预期的范围类型</param>
//	/// <param name="message"></param>
//	private void PopScope(Type expectedScopeType, string message)
//	{
//	    if (_scopeStack.Count == 0)
//	    {
//	        throw new InvalidOperationException("弹出范围的次数过多");
//	    }
//
//	    if (expectedScopeType != null && _currentScope.GetType() != expectedScopeType)
//	    {
//	        //如果 ?? 运算符的左操作数非空，该运算符将返回左操作数，否则返回右操作数。
//	        throw new InvalidOperationException(message ?? string.Format(
//	            "预计目前的范围类型为 '{0}' 但是实际是 '{1}'",
//	            expectedScopeType.Name, _currentScope.GetType().Name));
//	    }
//
//	    _currentScope.Dispose();
//	    _currentScope = _scopeStack.Pop();
//	}

	public Variable declare(Class<?> type, String name) {
		return _current.declare(type, name);
	}

	public Variable getVar(String name) {
		for (var scope : _scopes) {
			var local = scope.getVar(name);
			if (local != null)
				return local;
		}
		throw new IllegalArgumentException(strings("codeart", "VariableNotFound", name));
	}

	static class CodeScope implements AutoCloseable {

		private final MethodGenerator _owner;

		private final ArrayList<Variable> _locals;

		private Label _startLabel;

		public Label getStartLabel() {
			if (_startLabel == null)
				_startLabel = new Label();
			return _startLabel;
		}

		public void setStartLabel(Label label) {
			_startLabel = label;
		}

		private Label _endLabel;

		public Label getEndLabel() {
			if (_endLabel == null)
				_endLabel = new Label();
			return _endLabel;
		}

		public CodeScope(MethodGenerator owner) {
			_owner = owner;
			_locals = new ArrayList<Variable>();
		}

//		MethodGenerator getOwner() {
//			return _owner;
//		}

		public Variable declare(Class<?> type, String name) {
			var local = _owner.locals().borrow(type, name);
			local.setInScope(true);
			_locals.add(local);
			return local;
		}

//		public int getVarCount() {
//			return _locals.size();
//		}

		public Variable getVar(String name) {
			for (var local : _locals) {
				if (local.getName().equals(name))
					return local;
			}
			return null;
		}

		@Override
		public void close() {
			for (var local : _locals) {
				local.joinTable(this);
				local.close();
			}
		}
	}

}