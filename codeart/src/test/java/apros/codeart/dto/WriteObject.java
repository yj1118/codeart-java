package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 类尽量为无参数的，这样用dto加载最快
 */
class WriteObject {

	public static class NoArgumentUser {
		private int _id;

		public int getId() {
			return _id;
		}

		public void setId(int id) {
			_id = id;
		}

		private String _name;

		public String name() {
			return _name;
		}

		public void setName(String name) {
			_name = name;
		}

		public NoArgumentUser() {
		}

	}

	@Test
	public void noArgumentConstructorBaseField() {
		DTObject dto = DTObject.editable();
		dto.setInt("id", 1);
		dto.setString("name", "Louis");

		var user = dto.save(NoArgumentUser.class);

		assertEquals(1, user.getId());
		assertEquals("Louis", user.name());
	}

	public static class User {
		private int _id;

		public int getId() {
			return _id;
		}

		public void setId(int id) {
			_id = id;
		}

		private String _name;

		public String name() {
			return _name;
		}

		public void setName(String name) {
			_name = name;
		}

		public User(int id, String name, User father) {
			_id = id;
			_name = name;
			_father = father;
		}

		public User(int id, String name) {
			_id = id;
			_name = name;
		}

		private User _father;

		public User father() {
			return _father;
		}

	}

	@Test
	public void constructorBaseField() {
		DTObject dto = DTObject.editable();
		dto.setInt("id", 1);
		dto.setString("name", "Louis");

		DTObject dtoFather = DTObject.editable();
		dtoFather.setInt("id", 2);
		dtoFather.setString("name", "dahai");

		dto.setObject("father", dtoFather);

		var user = dto.save(User.class);

		assertEquals(1, user.getId());
		assertEquals("Louis", user.name());

		assertEquals(2, user.father().getId());
		assertEquals("dahai", user.father().name());
	}

}
