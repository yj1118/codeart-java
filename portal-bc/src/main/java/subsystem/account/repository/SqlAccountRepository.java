package subsystem.account.repository;

import apros.codeart.ddd.repository.access.SqlRepository;
import apros.codeart.util.SafeAccess;
import subsystem.account.Account;
import subsystem.account.IAccountRepository;

@SafeAccess
public class SqlAccountRepository extends SqlRepository<Account> implements IAccountRepository {

	@Override
	protected Class<? extends Account> getRootType() {
		return Account.class;
	}

}
