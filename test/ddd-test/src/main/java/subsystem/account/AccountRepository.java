package subsystem.account;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.ddd.repository.access.SqlRepository;
import apros.codeart.util.SafeAccess;
import subsystem.account.Account;
import subsystem.account.IAccountRepository;

@SafeAccess
public class AccountRepository extends SqlRepository<Account> implements IAccountRepository {

    @Override
    public Account findByIsEnabled(boolean isEnabled) {
        return DataPortal.querySingle(Account.class, "status.isEnabled=@isEnabled", (arg) ->
        {
            arg.put("isEnabled", isEnabled);
        }, QueryLevel.NONE);
    }


}
