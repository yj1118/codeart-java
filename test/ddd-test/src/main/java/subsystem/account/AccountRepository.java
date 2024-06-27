package subsystem.account;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.Page;
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

    @Override
    public Account findByIp(String ip) {
        return DataPortal.querySingle(Account.class, "status.loginInfo.lastIP=@ip", (arg) ->
        {
            arg.put("ip", ip);
        }, QueryLevel.NONE);
    }

    @Override
    public Iterable<Account> findsByIp(String ip){
        return DataPortal.query(Account.class, "status.loginInfo.lastIP=@ip[order by name]", (arg) ->
        {
            arg.put("ip", ip);
        }, QueryLevel.NONE);
    }

    @Override
    public int getCountByIp(String ip){
        return DataPortal.getCount(Account.class, "status.loginInfo.lastIP=@ip", (arg) ->
        {
            arg.put("ip", ip);
        }, QueryLevel.NONE);
    }

    @Override
    public Page<Account> finds(String name,int pageIndex,int pageSize){
        return DataPortal.query(Account.class, "name like %@name%",pageIndex,pageSize, (arg) ->
        {
            arg.put("name", name);
        });
    }

}
