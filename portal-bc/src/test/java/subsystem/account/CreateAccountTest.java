package subsystem.account;

import org.junit.jupiter.api.Test;

import subsystem.account.command.CreateAccount;

//@ExtendWith(TestRunner.class)
class CreateAccountTest {

	@Test
	void common() {
		var cmd = new CreateAccount("risan");
		cmd.execute();
	}

}
