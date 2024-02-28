package com.apros.codeart.bytecode;

import java.util.ArrayList;

class VariableCollection {
	private final MethodGenerator _owner;
	private final ArrayList<Variable> _items;

	private int _next_var_index = 0;

	public VariableCollection(MethodGenerator owner) {
		_owner = owner;
		_items = new ArrayList<Variable>();
		_next_var_index = owner.isStatic() ? 0 : 1; // 实例方法的第一个变量是this，所以实例方法的下一个变量序号为1
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
	public Variable borrow(Class<?> type, String name) {
		for (var item : _items) {
			if (!item.getInScope() && item.getType() == type) {
				item.setName(name); // 更新名称
				return item;
			}
		}
		var local = define(type, name, _next_var_index);
		_next_var_index++;
		return local;
	}

	/**
	 * 定义局部变量
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	private Variable define(Class<?> type, String name, int index) {
		var result = new Variable(_owner, type, name, index);
		_items.add(result);
		return result;
	}

}
