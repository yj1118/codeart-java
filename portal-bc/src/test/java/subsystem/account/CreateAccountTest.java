package subsystem.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import apros.codeart.ddd.launcher.TestLauncher;

//@ExtendWith(TestRunner.class)
public class CreateAccountTest {

	@BeforeAll
	public static void setup() {
		TestLauncher.start();
	}

	@AfterAll
	public static void clearn() {
		TestLauncher.stop();
	}

	@Test
	void common() {
		var call = Account.NameProperty.call();
		assertEquals("账户名", call);

//		var cmd = new CreateAccount("risan");
//		cmd.execute();
	}

}
