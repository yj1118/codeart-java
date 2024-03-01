package com.apros.codeart.bytecode;

public interface IVariable extends AutoCloseable {

	/**
	 * 获取变量类型
	 * 
	 * @return
	 */
	Class<?> getType();

	int getIndex();

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
	 * 栈顶的值存入到变量里
	 */
	void save();

	/**
	 * 类型转换
	 */
	IVariable cast(Class<?> targetType);

	/**
	 * 发出赋值给该变量的指令块的开始,需要在 BeginAssign 和 EndAssign 之间加载需要赋值的值
	 */
	void beginAssign();

	/**
	 * 结束分配
	 */
	void endAssign();
}
