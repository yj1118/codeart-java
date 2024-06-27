package subsystem.account;

import apros.codeart.ddd.IRepository;

public interface IAccountRepository extends IRepository<Account> {
    Account findByIsEnabled(boolean isEnabled);

    Account findByIp(String ip);

    Iterable<Account> findsByIp(String ip);
}
