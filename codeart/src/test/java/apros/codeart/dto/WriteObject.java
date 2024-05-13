package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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

		public User(int id, String name) {
			_id = id;
			_name = name;
		}

	}

	@Test
	public void constructorBaseField() {
		DTObject dto = DTObject.editable();
		dto.setInt("id", 1);
		dto.setString("name", "Louis");

		var user = dto.save(User.class);

		assertEquals(1, user.getId());
		assertEquals("Louis", user.name());
	}

}
