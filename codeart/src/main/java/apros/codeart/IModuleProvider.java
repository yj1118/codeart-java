package apros.codeart;

/**
 * 模块提供者，该接口负责对某一类模块提供安装
 */
public interface IModuleProvider {

	/**
	 * 
	 * 提供者的名称
	 * 
	 * @return
	 */
	String name();

	/**
	 * 安装模块
	 */
	void setup();
}
