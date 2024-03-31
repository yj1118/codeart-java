package service.portal;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.runtime.Activator;

public class Program {

	public static void main(String[] args) {

		var types = Activator.<DomainObject>getSubTypesOf(DomainObject.class, "subsystem", "service");

		for (var type : types) {
			System.out.print(type.getName());
		}
	}
}
