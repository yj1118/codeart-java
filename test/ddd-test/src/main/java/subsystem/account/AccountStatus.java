package subsystem.account;

import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.EntityObjectLong;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.validation.TimePrecision;
import apros.codeart.ddd.validation.TimePrecisions;

import java.time.LocalDateTime;

public class AccountStatus extends EntityObjectLong {


    @PropertyRepository
    public static final DomainProperty IsEnabledProperty = DomainProperty.register("IsEnabled", boolean.class, AccountStatus.class, (obj, pro) -> {
        return true;
    });

    public boolean isEnabled() {
        return this.getValue(IsEnabledProperty, boolean.class);
    }

    /**
     * 是否启用
     */
    public void isEnabled(boolean value) {
        this.setValue(IsEnabledProperty, value);
    }

    //region 登录信息

    @PropertyRepository
    private static final DomainProperty LoginInfoProperty = DomainProperty.register("LoginInfo", LoginInfo.class, AccountStatus.class);

    /**
     * 登录信息
     */
    public LoginInfo loginInfo() {
        return this.getValue(LoginInfoProperty, LoginInfo.class);
    }

    private void loginInfo(LoginInfo value) {
        this.setValue(LoginInfoProperty, value);
    }

    /**
     * 更新登录信息
     */
    public void updateLogin(String lastIp) {
        var info = this.loginInfo().update(lastIp);
        this.loginInfo(info);
    }

    //endregion

    
    @ConstructorRepository
    public AccountStatus(long id, LoginInfo loginInfo) {
        super(id);
        this.loginInfo(loginInfo);
        this.onConstructed();
    }

    //region 空对象

    private static class AccountStatusEmpty extends AccountStatus {
        public AccountStatusEmpty() {
            super(0, LoginInfo.empty());
            this.onConstructed();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        public static final AccountStatusEmpty INSTANCE = new AccountStatusEmpty();
    }

    public static AccountStatus empty() {
        return AccountStatusEmpty.INSTANCE;
    }


    //endregion

}
