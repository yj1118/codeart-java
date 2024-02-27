package com.apros.codeart.bytecode;

import java.util.ArrayList;

class LocalDefinitionCollection {
	private final MethodGenerator _owner;
	private final ArrayList<LocalDefinition> _items;

	private int _varIndex = 0;

	public LocalDefinitionCollection(MethodGenerator owner, Iterable<Argument> args) {
		_owner = owner;
		_items = new ArrayList<LocalDefinition>();

		_varIndex = owner.isStatic() ? 0 : 1; // 实例方法的第一个变量是this
		for (var arg : args) {
			_varIndex++;
			define(arg.getType(), arg.getName(), _varIndex);
		}
	}

	/**
	 * 从已定义的变量集合中，借一个局部变量
	 * 
	 * 可以借出的变量，需要满足2个条件：1.变量已失效（在声明代码块范围之外）2.变量类型相同（类型要相同）
	 * 
	 * 借出机制是为了节省创建变量的内存消耗，对已创建过的变量进行缓存，重复利用
	 * 
	 * 注意，这里的节省是字节代码实际运行时的节省
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	public BorrowedLocal borrow(Class<?> type, String name) {
		for (var item : _items) {
			if (!item.getInScope() && item.getType() == type) {
				return new BorrowedLocal(item);
			}
		}
		_varIndex++;
		return new BorrowedLocal(define(type, name, _varIndex));
	}

	/**
	 * 定义局部变量
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	private LocalDefinition define(Class<?> type, String name, int index) {
		var result = new LocalDefinition(_owner, type, name, index);
		_items.add(result);
		return result;
	}

}
