package subsystem.account;

import apros.codeart.ddd.AggregateRootLong;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.repository.PropertyRepository;

public class Account extends AggregateRootLong {

	@PropertyRepository()
	@NotEmpty()
	@StringLength(1,15)
	static final DomainProperty NameProperty = DomainProperty.register("name", String.class, Account.class);

	/**
	 * 
	 * 账户名
	 * 
	 * @return
	 */
	public String name() {
		return this.getValue(NameProperty, String.class);
	}

	public void setName(String value) {
		this.setValue(NameProperty, value);
	}

	public Account(long id) {
		super(id);
	}

}
