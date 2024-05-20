package subsystem.account.command;

import apros.codeart.ddd.command.Callable;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.repository.access.DataPortal;
import subsystem.account.Account;
import subsystem.account.IAccountRepository;

public class CreateAccount extends Callable<Account> {

	private String _name;

	public String email;

	public CreateAccount(String name) {
		_name = name;
	}

	@Override
	protected Account executeImpl() {

		long id = DataPortal.getIdentity(Account.class);

		Account acc = new Account(id, _name);

		if (email != null)
			acc.email(_name);

		var repository = Repository.create(IAccountRepository.class);
		repository.add(acc);

		return acc;
	}

}
