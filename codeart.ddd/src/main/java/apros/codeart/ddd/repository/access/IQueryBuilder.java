package apros.codeart.ddd.repository.access;

public interface IQueryBuilder {

	/**
	 * 
	 * 构建执行语句，在这个过程中有可能改变description里的值，特别是参数
	 *
	 */
	String build(QueryDescription description);


//	/**
//	 * 为支持该查询所初始化的资源
//	 */
//	void init();
//
//	/**
//	 * 清理资源，该方法一般用于测试或者构建发布时用
//	 */
//	void clearUp();
}
