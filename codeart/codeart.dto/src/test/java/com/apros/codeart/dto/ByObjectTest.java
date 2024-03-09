package com.apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
	}

	@Test
	public void Common() {
		var user = new User(1, "Louis");
		DTObject dto = DTObject.readonly(user);

		assertEquals(1, dto.getInt("id"));
	}

}
