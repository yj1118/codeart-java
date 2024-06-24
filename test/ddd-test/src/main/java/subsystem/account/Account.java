package subsystem.account;

import static apros.codeart.runtime.Util.propagate;

import java.time.LocalDateTime;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import apros.codeart.UIException;
import apros.codeart.i18n.Language;
import com.google.common.primitives.Longs;

import apros.codeart.ddd.AggregateRootLong;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.PropertyLabel;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.validation.ASCIIString;
import apros.codeart.ddd.validation.Email;
import apros.codeart.ddd.validation.NotEmpty;
import apros.codeart.ddd.validation.StringLength;
import apros.codeart.ddd.validation.TimePrecision;
import apros.codeart.ddd.validation.TimePrecisions;
import apros.codeart.pooling.util.StringPool;
import apros.codeart.util.StringUtil;

public class Account extends AggregateRootLong {

    //region 账户名

    @PropertyRepository()
    @NotEmpty()
    @StringLength(min = 1, max = 15)
    @PropertyLabel("@accountName")
    public static final DomainProperty NameProperty = DomainProperty.register("Name", String.class, Account.class);

    /**
     * 账户名
     */
    public String name() {
        return this.getValue(NameProperty, String.class);
    }

    public void name(String value) {
        this.setValue(NameProperty, value);
    }

    //endregion

    //region 电子邮箱

    /**
     * 电子邮箱
     */
    @PropertyRepository()
    @Email()
    @StringLength(max = 300)
    @PropertyLabel("メール")
    public static final DomainProperty EmailProperty = DomainProperty.register("Email", String.class, Account.class);

    public String email() {
        return this.getValue(EmailProperty, String.class);
    }

    public void email(String value) {
        this.setValue(EmailProperty, value);
    }

    /**
     * 邮箱是否已认证
     */
    @PropertyRepository()
    public static final DomainProperty EmailVerifiedProperty = DomainProperty.register("EmailVerified", boolean.class,
            Account.class);

    public boolean emailVerified() {
        return this.getValue(EmailProperty, boolean.class);
    }

    public void emailVerified(boolean value) {
        this.setValue(EmailProperty, value);
    }


    //endregion

    //region 密码

    @PropertyRepository()
    @NotEmpty()
    @ASCIIString()
    @StringLength(max = 200)
    private static final DomainProperty PasswordProperty = DomainProperty.register("Password", String.class,
            Account.class);

    /**
     * 密码
     */
    public String password() {
        return this.getValue(PasswordProperty, String.class);
    }

    private void password(String value) {
        this.setValue(PasswordProperty, value);
    }

    /**
     * 设置并且加密密码
     */
    public void setPasswordAndEncrypt(String password) {
        if (this.password().equals(password))
            return; // password已被加密，且等同于当前密码，不必修改
        var value = pbkdf2(password, Longs.toByteArray(this.id()), 100); // 使用ID的字节数组作为密码盐
        this.password(value);
    }

    private static String pbkdf2(String str, byte[] salt, int length) {
        try {
            int iterations = 1000; // 建议的最小迭代次数
            PBEKeySpec spec = new PBEKeySpec(str.toCharArray(), salt, iterations, length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return bytesToHex(hash);
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        return StringPool.using((sb) -> {
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
        });
    }

    /**
     * 检查密码是否正确
     */
    public boolean validatePassword(String value) {
        var pwd = this.password();
        return pwd.equals(value) || pwd.equals(pbkdf2(value, Longs.toByteArray(this.id()), 100));
    }

    //endregion

    //region 创建时间

    @PropertyRepository
    @TimePrecision(TimePrecisions.Second)
    private static final DomainProperty CreateTimeProperty = DomainProperty.register("CreateTime", LocalDateTime.class,
            Account.class, (obj, pro) -> {
                return LocalDateTime.now();
            });

    public LocalDateTime createTime() {
        return this.getValue(CreateTimeProperty, LocalDateTime.class);
    }

    private void createTime(LocalDateTime value) {
        this.setValue(CreateTimeProperty, value);
    }


    //endregion

    //region 状态

    @PropertyRepository(lazy = true)
    @NotEmpty
    private static final DomainProperty StatusProperty = DomainProperty.register("Status", AccountStatus.class, Account.class);

    /**
     * 账号的系统状态
     *
     * @return
     */
    public AccountStatus status() {
        return this.getValue(StatusProperty, AccountStatus.class);
    }

    private void status(AccountStatus value) {
        this.setValue(StatusProperty, value);
    }

    //endregion


    public void login(String Ip) {
        if (!this.status().isEnabled()) throw new UIException(Language.strings("AccountDisabled"));
        this.status().updateLogin(Ip);
    }

    public Account(long id, String name) {
        super(id);
        this.name(name);
        this.onConstructed();
    }

    @ConstructorRepository
    Account(long id, LocalDateTime createTime) {
        super(id);
        this.createTime(createTime);
        this.onConstructed();
    }

    //region 空对象

    private static class AccountEmpty extends Account {

        public AccountEmpty() {
            super(0, StringUtil.empty());
            this.onConstructed();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        public static final AccountEmpty INSTANCE = new AccountEmpty();
    }

    public static Account empty() {
        return AccountEmpty.INSTANCE;
    }


    //endregion

}
