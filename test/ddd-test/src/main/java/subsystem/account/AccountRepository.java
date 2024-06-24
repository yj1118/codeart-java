package subsystem.account;

import apros.codeart.ddd.repository.access.SqlRepository;
import apros.codeart.util.SafeAccess;
import subsystem.account.Account;
import subsystem.account.IAccountRepository;

@SafeAccess
public class AccountRepository extends SqlRepository<Account> implements IAccountRepository {

}
