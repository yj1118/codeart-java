package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

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

	@Test
	public void constructorList() {
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

	public static class Menu {
		private int _id;

		public int id() {
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

		public Menu(int id, String name) {
			_id = id;
			_name = name;
			_childs = new ArrayList<Menu>();
		}

		private Menu _parent;

		public Menu parent() {
			return _parent;
		}

		private ArrayList<Menu> _childs;

		public Iterable<Menu> childs() {
			return _childs;
		}

		public void seChilds(Iterable<Menu> value) {
			_childs = value;
		}

		public Menu addChild(int id, String name) {
			var child = new Menu(id, name);
			_childs.add(child);
			return child;
		}

	}

	@Test
	public void writeList() {

		var dtoMenu = createMenu();

	}

	private static DTObject createMenu() {
		var root = DTObject.editable();
		root.setInt("id", 1);
		root.setString("name", "根菜单");

		var home = DTObject.editable();
		home.setInt("id", 2);
		home.setString("name", "根首页");
		root.push("childs,", home);

		var sys = DTObject.editable();
		sys.setInt("id", 3);
		sys.setString("name", "系统设置");
		root.push("childs,", sys);

		var p = DTObject.editable();
		p.setInt("id", 4);
		p.setString("name", "权限管理");
		sys.push("childs,", p);

		var s = DTObject.editable();
		s.setInt("id", 5);
		s.setString("name", "皮肤设定");
		sys.push("childs,", s);

		var us = DTObject.editable();
		us.setInt("id", 6);
		us.setString("name", "关于我们");
		sys.push("childs,", us);

		return root;
	}

}
