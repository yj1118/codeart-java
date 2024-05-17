package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

/**
 * 类尽量为无参数的，这样用dto加载最快
 */
@SuppressWarnings({ "exports" })
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

		public Menu() {
			_childs = new ArrayList<Menu>();
		}

		public Menu(int id, String name) {
			_id = id;
			_name = name;
			_childs = new ArrayList<Menu>();
		}

		private ArrayList<Menu> _childs;

		public Iterable<Menu> childs() {
			return _childs;
		}

		public void setChilds(ArrayList<Menu> value) {
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

		var root = dtoMenu.save(Menu.class);
		assertEquals(1, root.id());
		assertEquals("根菜单", root.name());

		assertEquals(2, Iterables.size(root.childs()));

		var home = Iterables.get(root.childs(), 0);
		assertEquals(2, home.id());
		assertEquals("首页", home.name());

		var sys = Iterables.get(root.childs(), 1);
		assertEquals(3, sys.id());
		assertEquals("系统设置", sys.name());

		var p = Iterables.get(sys.childs(), 0);
		assertEquals(4, p.id());
		assertEquals("权限管理", p.name());

		var s = Iterables.get(sys.childs(), 1);
		assertEquals(5, s.id());
		assertEquals("皮肤设定", s.name());

		var us = Iterables.get(sys.childs(), 2);
		assertEquals(6, us.id());
		assertEquals("关于我们", us.name());

	}

	private static DTObject createMenu() {
		var root = DTObject.editable();
		root.setInt("id", 1);
		root.setString("name", "根菜单");

		var home = DTObject.editable();
		home.setInt("id", 2);
		home.setString("name", "首页");
		root.push("childs", home);

		var sys = DTObject.editable();
		sys.setInt("id", 3);
		sys.setString("name", "系统设置");
		root.push("childs", sys);

		var p = DTObject.editable();
		p.setInt("id", 4);
		p.setString("name", "权限管理");
		sys.push("childs", p);

		var s = DTObject.editable();
		s.setInt("id", 5);
		s.setString("name", "皮肤设定");
		sys.push("childs", s);

		var us = DTObject.editable();
		us.setInt("id", 6);
		us.setString("name", "关于我们");
		sys.push("childs", us);

		return root;
	}

	public static class MenuA {
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

		public MenuA(int id, String name) {
			_id = id;
			_name = name;
		}

		private MenuA[] _childs;

		public MenuA[] childs() {
			return _childs;
		}

		public void setChilds(MenuA[] value) {
			_childs = value;
		}

	}

	@Test
	public void writeArray() {

		var dtoMenu = createMenu();

		var root = dtoMenu.save(MenuA.class);
		assertEquals(1, root.id());
		assertEquals("根菜单", root.name());

		assertEquals(2, root.childs().length);

		var home = root.childs()[0];
		assertEquals(2, home.id());
		assertEquals("首页", home.name());

		var sys = root.childs()[1];
		assertEquals(3, sys.id());
		assertEquals("系统设置", sys.name());

		var p = sys.childs()[0];
		assertEquals(4, p.id());
		assertEquals("权限管理", p.name());

		var s = sys.childs()[1];
		assertEquals(5, s.id());
		assertEquals("皮肤设定", s.name());

		var us = sys.childs()[2];
		assertEquals(6, us.id());
		assertEquals("关于我们", us.name());

	}

}
