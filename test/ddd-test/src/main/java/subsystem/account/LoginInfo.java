package subsystem.account;

import apros.codeart.ddd.ValueObject;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.validation.ASCIIString;
import apros.codeart.ddd.validation.StringLength;
import apros.codeart.util.StringUtil;

import java.time.LocalDateTime;

import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.Emptyable;
import apros.codeart.ddd.EmptyableDateTime;

public class LoginInfo extends ValueObject {

    @PropertyRepository
    @ASCIIString
    @StringLength(max = 50)
    private static final DomainProperty LastIPProperty = DomainProperty.register("LastIP", String.class, LoginInfo.class);


    /**
     * 登录时使用的IP地址
     *
     * @return
     */
    public String lastIP() {
        return this.getValue(LastIPProperty, String.class);
    }

    private void lastIP(String value) {
        this.setValue(LastIPProperty, value);
    }


    @PropertyRepository
    private static final DomainProperty LastTimeProperty = DomainProperty.register("LastTime", EmptyableDateTime.class, LoginInfo.class);


    /**
     * 最后一次登录时间
     *
     * @return
     */
    public Emptyable<LocalDateTime> lastTime() {
        return this.getValue(LastTimeProperty, EmptyableDateTime.class);
    }

    private void lastTime(Emptyable<LocalDateTime> value) {
        this.setValue(LastTimeProperty, value);
    }

    @PropertyRepository
    private static final DomainProperty TotalProperty = DomainProperty.register("Total", int.class, LoginInfo.class);


    /**
     * 总登录次数
     *
     * @return
     */
    public int total() {
        return this.getValue(TotalProperty, int.class);
    }

    private void total(int value) {
        this.setValue(TotalProperty, value);
    }


    /**
     * 更新登录信息，登录次数会增加，最后一次登录时间和登录IP会被覆盖
     *
     * @param lastIP
     * @return
     */
    public LoginInfo update(String lastIP) {
        if (this.isEmpty()) return new LoginInfo(lastIP, EmptyableDateTime.now(), 1);
        return new LoginInfo(lastIP, EmptyableDateTime.now(), this.total() + 1);
    }

    @ConstructorRepository()
    public LoginInfo(String lastIP, EmptyableDateTime lastTime, int total) {
        this.lastIP(lastIP);
        this.lastTime(lastTime);
        this.total(total);
        this.onConstructed();
    }

    //region 空对象

    private static class LoginInfoEmpty extends LoginInfo {
        public LoginInfoEmpty() {
            super(StringUtil.empty(), EmptyableDateTime.createEmpty(), 0);
            this.onConstructed();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }


        public static final LoginInfoEmpty INSTANCE = new LoginInfoEmpty();
    }

    public static LoginInfo empty() {
        return LoginInfoEmpty.INSTANCE;
    }

    //endregion
}
