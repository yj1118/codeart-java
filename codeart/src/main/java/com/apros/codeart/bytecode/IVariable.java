package com.apros.codeart.bytecode;

interface IVariable {

	/**
	 * 获取变量类型
	 * 
	 * @return
	 */
	Class<?> getType();

	/**
	 * 变量名称，当变量为nul，该属性无效
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 发出一个指令，加载变量的值到堆栈
	 * 
	 * @param options
	 */
	void load();

	/**
	 * 将位于指定地址的对象的所有字段初始化为空引用或基元类型的 0。
	 */
	void initialize();

//	/**
//	 * 发出存储变量的指令
//	 */
//	void store();
//
//	/// <summary>
//	/// 获取一个值，指示实例是否能够使用<see cref="Store"/> 方法
//	/// </summary>
//	bool CanStore
//	{ get; }

	/**
	 * 发出赋值给该变量的指令块的开始,需要在 BeginAssign 和 EndAssign 之间加载需要赋值的值
	 */
	void beginAssign();

	/**
	 * 结束分配
	 */
	void endAssign();
}
