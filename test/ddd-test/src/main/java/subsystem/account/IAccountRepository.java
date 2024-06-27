package subsystem.account;

import apros.codeart.ddd.IRepository;
import apros.codeart.ddd.repository.Page;

public interface IAccountRepository extends IRepository<Account> {
    Account findByIsEnabled(boolean isEnabled);

    Account findByIp(String ip);

    Iterable<Account> findsByIp(String ip);

    int getCountByIp(String ip);

    Page<Account> findPage(String name,int pageIndex,int pageSize);

    Page<Account> findPageByIp(String ip,int pageIndex,int pageSize);

}
