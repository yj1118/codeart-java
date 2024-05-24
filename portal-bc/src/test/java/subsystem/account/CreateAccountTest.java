package subsystem.account;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import apros.codeart.i18n.Language;

//@ExtendWith(TestRunner.class)
public class CreateAccountTest {

	@BeforeAll
	public static void setup() {
		var str = Language.strings("apros.codeart.ddd", "DataTypeNotSupported", "xxx");
//		TestLauncher.start();
//		System.out.println(System.getProperty("java.class.path"));
		// 使用ClassLoader手动查找资源文件
//		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//		if (classLoader.getResource("codeart/ddd/strings.properties") != null) {
//			System.out.println("手动找到 codeart/ddd/strings.properties");
//		} else {
//			System.out.println("手动未找到 codeart/ddd/strings.properties");
//		}
////		
//		try {
////			ResourceBundle bundle1 = ResourceBundle.getBundle("codeart.strings");
////			System.out.println("codeart.strings 加载成功");
//
//			ResourceBundle bundle2 = ResourceBundle.getBundle("codeart.ddd.strings");
//			System.out.println("codeart.ddd.strings 加载成功");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@AfterAll
	public static void clearn() {
//		TestLauncher.stop();
	}

	@Test
	void common() {

		System.out.println(System.getProperty("java.class.path"));

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader.getResource("codeart/strings.properties") != null) {
			System.out.println("手动找到 codeart/strings.properties");
		} else {
			System.out.println("手动未找到 codeart/ddd/strings.properties");
		}

//		var name = Account.NameProperty.call();
//		var cmd = new CreateAccount("risan");
//		cmd.execute();
	}

}
