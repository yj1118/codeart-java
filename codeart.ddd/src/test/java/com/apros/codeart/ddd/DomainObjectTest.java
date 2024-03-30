package com.apros.codeart.ddd;

import org.junit.Test;

public class DomainObjectTest {

	@ObjectValidator(UserSpec.class)
	public static class User {

//		@StringLength(0, 100)
//        @ASCIIString()
//		private static final DomainProperty NameProperty = DomainProperty.register<String,User>("name");
//
//		public String name() {
//			return getValue(NameProperty, String.class);
//		}
//
//		public String name(String value) {
//			return setValue(NameProperty, value);
//		}

//		public static final DomainProperty NameProperty= DomainProperty.register<String,User>("name");

//		/// <summary>
//		/// 消费卡余额
//		/// </summary>
//		public decimal Value
//		{
//            get
//            {
//                return GetValue<decimal>(ValueProperty);
//            }
//            private set
//            {
//                SetValue(ValueProperty, value);
//            }
//        }

//		@Property
//		private DomainCollection<UserTeam> _temas;

	}

	private static class UserSpec extends ObjectValidatorImpl {

		@Override
		public void validate(IDomainObject obj, ValidationResult result) {
			// TODO Auto-generated method stub

		}

	}

	@Test
	public void getObject() {

//		var user = DomainContainer.createObject(User.class,);
//		
//		user.name();
//		

	}

}
