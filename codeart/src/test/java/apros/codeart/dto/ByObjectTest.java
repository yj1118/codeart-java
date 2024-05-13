package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

import apros.codeart.util.ListUtil;

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
	public void baseField() {
		var user = new User(1, "Louis");

		DTObject dto = DTObject.readonly(user);

		assertEquals(1, dto.getInt("id"));
		assertEquals("Louis", dto.getString("name"));
	}

	@Test
	public void byRefObject() {
		var user = new User(1, "Louis");
		user.setBrother(new User(2, "dahai"));

		DTObject dto = DTObject.readonly(user);

		var brother = dto.getObject("brother");

		assertEquals(2, brother.getInt("id"));
		assertEquals("dahai", brother.getString("name"));

		var code = brother.getCode(false, false);
		assertEquals("{\"id\":2,\"name\":\"dahai\"}", code);
	}

	@Test
	public void partField() {
		var user = new User(1, "Louis");
		DTObject dto = DTObject.readonly("{id}", user);

		assertEquals(1, dto.getInt("id"));
		assertFalse(dto.exist("name"));
	}

	@Test
	public void editObject() {
		var user = new User(1, "Louis");
		var dtoUser = DTObject.editable(user);

		dtoUser.setString("name", "xiaoli");

		assertEquals("xiaoli", dtoUser.getString("name"));
	}

	@Test
	public void editbyRefObject() {
		var user = new User(1, "Louis");
		user.setBrother(new User(2, "dahai"));

		var dtoUser = DTObject.editable(user);
		var brother = dtoUser.getObject("brother");

		brother.setString("name", "mori");

		assertEquals("mori", brother.getString("name"));
	}

	@Test
	public void editReadonly() {
		var user = new User(1, "Louis");
		var dtoUser = DTObject.readonly(user);

		Assertions.assertThrows(DTOReadonlyException.class, () -> {
			dtoUser.setString("name", "xiaoli");
		});

	}

	@Test
	public void setObject() {
		var user = new User(1, "Louis");
		var dtoUser = DTObject.readonly("{id,name}", user);

		DTObject dto = DTObject.editable("{user}");
		dto.setObject("user", dtoUser);

		var result = dto.getObject("user");

		assertEquals(1, result.getInt("id"));
		assertEquals("Louis", result.getString("name"));
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

		public Menu addChild(int id, String name) {
			var child = new Menu(id, name);
			_childs.add(child);
			return child;
		}

	}

	@Test
	public void readList() {
		var menu = new Menu(1, "根菜单");
		menu.addChild(2, "首页");
		var setting = menu.addChild(3, "系统设置");
		setting.addChild(4, "权限管理");
		setting.addChild(5, "皮肤设定");
		setting.addChild(6, "关于我们");

		var dtoMenu = DTObject.readonly(menu);
		assertEquals(1, dtoMenu.getInt("id"));
		assertEquals("根菜单", dtoMenu.getString("name"));

		var rootChilds = ListUtil.asList(dtoMenu.getObjects("childs"));
		assertEquals(2, rootChilds.size());

		var home = rootChilds.get(0);
		assertEquals(2, home.getInt("id"));
		assertEquals("首页", home.getString("name"));
		assertEquals(0, Iterables.size(home.getObjects("childs")));

		var sys = rootChilds.get(1);
		assertEquals(3, sys.getInt("id"));
		assertEquals("系统设置", sys.getString("name"));

		var sysChilds = ListUtil.asList(sys.getObjects("childs"));
		assertEquals(3, sysChilds.size());

		var p = sysChilds.get(0);
		assertEquals(4, p.getInt("id"));
		assertEquals("权限管理", p.getString("name"));

		var s = sysChilds.get(1);
		assertEquals(5, s.getInt("id"));
		assertEquals("皮肤设定", s.getString("name"));

		var us = sysChilds.get(2);
		assertEquals(6, us.getInt("id"));
		assertEquals("关于我们", us.getString("name"));

	}

}
