package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class ByObjectTest {

	public static class User {
		private int _id;

		public int getId() {
			return _id;
		}

		public void setId(int id) {
			_id = id;
		}

		private String _name;

		public String getName() {
			return _name;
		}

		public void setName(String name) {
			_name = name;
		}

		public User(int id, String name) {
			_id = id;
			_name = name;
		}

		public User _parent;

		public User parent() {
			return _parent;
		}

		private User _brother;

		public User brother() {
			return _brother;
		}

		public void setBrother(User value) {
			_brother = value;
		}

	}

	@Test
	public void Common1() {
		var user = new User(1, "Louis");
		DTObject dto = DTObject.readonly(user);

		assertEquals(1, dto.getInt("id"));

		assertEquals(null, dto.getObject("parent", null));

	}

	@Test
	public void Common2() {
		var user = new User(1, "Louis");
		DTObject dto = DTObject.readonly("{id}", user);

		assertEquals(1, dto.getInt("id"));
		assertFalse(dto.exist("name"));

	}

	@Test
	public void editObject() {
		var user = new User(1, "Louis");
		DTObject dto = DTObject.editable("{user}");
		var dtoUser = DTObject.readonly("{id,name}", user);
		dto.setObject("user", dtoUser);

		var result = dto.getObject("user");

		assertEquals(1, result.getInt("id"));
		assertEquals("Louis", result.getString("name"));
	}

}
