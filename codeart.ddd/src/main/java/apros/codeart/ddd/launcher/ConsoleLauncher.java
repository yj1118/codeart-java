package apros.codeart.ddd.launcher;

import apros.codeart.ddd.service.ServiceHost;

/**
 * 基于命令行的启动器
 */
public class ConsoleLauncher {

	public static void main(String[] args) {
		ServiceHost.start();
	}

}
