//package com.apros.codeart.bytecode;
//
//import java.util.ArrayList;
//
//class LocalDefinitionCollection {
//	private final MethodGenerator _owner;
//	private final ArrayList<LocalDefinition> _items;
//
//	public LocalDefinitionCollection(MethodGenerator owner) {
//		_owner = owner;
//		_items = new ArrayList<LocalDefinition>();
//	}
//
//	/// <summary>
//	/// <para>从已定义的变量集合中，借一个局部变量</para>
//	/// <para>可以借出的变量，需要满足2个条件：1.变量已失效（在声明代码块范围之外）2.变量类型相同（类型要相同）</para>
//	/// <para>借出机制是为了节省创建变量的内存消耗，对已创建过的变量进行缓存，重复利用</para>
//	/// </summary>
//	/// <param name="type"></param>
//	/// <param name="isPinned"></param>
//	/// <param name="name"></param>
//	/// <returns></returns>
//	public BorrowedLocal borrow(Class<?> type, String name) {
//		for (var item : _items) {
//			if (!item.getInScope() && item.getType() == type) {
//				return new BorrowedLocal(item);
//			}
//		}
//		return new BorrowedLocal(define(type, name));
//	}
//
//	/// <summary>
//	/// 定义局部变量
//	/// </summary>
//	/// <param name="type">变量类型</param>
//	/// <param name="isPinned">变量是否是固定的</param>
//	/// <param name="name">变量名称</param>
//	/// <returns>返回局部变量的包装</returns>
//	private LocalDefinition define(Class<?> type, String name) {
//		var result = new LocalDefinition(_owner, _writer.DefineLocal(type, isPinned, name));
//		_locals.Add(result);
//		return result;
//	}
//
//}
