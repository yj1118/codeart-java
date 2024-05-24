package subsystem.account;

import apros.codeart.ddd.AggregateRootLong;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.PropertyLabel;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.validation.Email;
import apros.codeart.ddd.validation.NotEmpty;
import apros.codeart.ddd.validation.StringLength;
import apros.codeart.util.StringUtil;

public class Account extends AggregateRootLong {
	@PropertyRepository()
	@NotEmpty()
	@StringLength(min = 1, max = 15)
	@PropertyLabel("accountName")
	static final DomainProperty NameProperty = DomainProperty.register("Name", String.class, Account.class);

	/**
	 * 
	 * 账户名
	 * 
	 * @return
	 */
	public String name() {
		return this.getValue(NameProperty, String.class);
	}

	public void name(String value) {
		this.setValue(NameProperty, value);
	}

	/**
	 * 电子邮箱
	 */
	@PropertyRepository()
	@Email()
	@StringLength(max = 300)
	static final DomainProperty EmailProperty = DomainProperty.register("Email", String.class, Account.class);

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
	private static final DomainProperty EmailVerifiedProperty = DomainProperty.register("EmailVerified", boolean.class,
			Account.class);

	public boolean emailVerified() {
		return this.getValue(EmailProperty, boolean.class);
	}

	public void emailVerified(boolean value) {
		this.setValue(EmailProperty, value);
	}

	public Account(long id, String name) {
		super(id);
		this.name(name);
		this.onConstructed();
	}

	@ConstructorRepository
	Account(long id) {
		super(id);
		this.onConstructed();
	}

//	#region 空对象

	public class AccountEmpty extends Account {

		public AccountEmpty() {
			super(0, StringUtil.empty());
			this.onConstructed();
		}

		@Override
		public boolean isEmpty() {
			return true;
		}
	}

//	#endregion

}
