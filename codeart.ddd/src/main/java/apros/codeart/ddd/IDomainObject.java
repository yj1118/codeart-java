package apros.codeart.ddd;

public interface IDomainObject extends INotNullObject, IStateObject, ISupportFixedRules {

	/**
	 * 对象验证器
	 * 
	 * @return
	 */
	Iterable<IObjectValidator> validators();

	/**
	 * 
	 * 对象是否发生了改变
	 * 
	 * @return
	 */
	boolean isChanged();

	/**
	 * 对象是否为一个快照
	 * 
	 * @return
	 */
	boolean isSnapshot();

	/**
	 * 属性是否发生改变
	 * 
	 * @param property
	 * @return
	 */
	boolean isPropertyChanged(DomainProperty property);

	/**
	 * 
	 * 属性还是否为脏的（属性没有发生改变，不一定不是脏的，例如：引用关系没变，属性也没变，但是有可能属性内部的值发生了改变）
	 * 
	 * @param property
	 * @return
	 */
	boolean isPropertyDirty(DomainProperty property);

	/**
	 * 
	 * 对象的数据版本号
	 * 
	 * @return
	 */
	int dataVersion();

}