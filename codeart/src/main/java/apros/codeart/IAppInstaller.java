package apros.codeart;

/**
 * 设计该接口的初衷：
 * 
 * 1.在框架内部让各种配置可以按照运行的顺序有序加载
 * 
 * 2.在内部和外部都可以统一在一个地方维护配置管理的编码
 * 
 * 3.实现编码式配置和文件式配置
 */
public interface IAppInstaller {

	/**
	 * 初始化安装器
	 */
	void init();

	String[] getArchives();

	void setup(String moduleName, Object[] args);

	/**
	 * 安装器执行完毕后，清理资源
	 */
	void dispose();

}
