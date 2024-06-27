package subsystem.account;

import apros.codeart.ddd.IRepository;
import apros.codeart.ddd.repository.Page;

public interface IAccountRepository extends IRepository<Account> {
    Account findByIsEnabled(boolean isEnabled);

    Account findByIp(String ip);

    Iterable<Account> findsByIp(String ip);

    int getCountByIp(String ip);

    Page<Account> finds(String name,int pageIndex,int pageSize);

}
