package subsystem.account;

import apros.codeart.ddd.repository.access.SqlRepository;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class AccountRepository extends SqlRepository<Account> implements IAccountRepository {

}
