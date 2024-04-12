package com.apros.codeart.ddd;

import java.util.UUID;

/**
 * 当我们只关心一个模型的属性时，应把它归类为Value Object。
 * 
 * 我们应该使这个模型元素能够表示出其属性的意义，并为它提供相关功能。 Value
 * 
 * Object应该是不可变的。不要为它分配任何标示，而且不要把它设计成像Entity那么复杂。
 * 
 */
public interface IValueObject extends IDomainObject {

	/**
	 * 用于持久化的编号，不要在领域模型中使用它
	 */
	UUID getPersistentIdentity();

	void setPersistentIdentity(UUID value);
}
