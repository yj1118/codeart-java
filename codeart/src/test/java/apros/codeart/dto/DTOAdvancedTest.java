package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public class DTOAdvancedTest {

	@Test
	public void assignCommon() {
		var code = "{\"id\":13,\"markedCode\":\"test\",\"name\":\"测试\",person:{\"name\":\"张三\"},\"orderIndex\":1,\"config\":[{\"name\":\"name0\",\"sex\":1},{\"name\":\"name1\",\"sex\":0}],\"description\":\"类型描述\"}";
		DTObject dto = DTObject.editable(code);
		dto.transform("id=id", (id) -> {
			return 15;
		});

		dto.transform("person.name=name", (name) -> {
			return "李四";
		});

		dto.transform("config.name=name", (v) -> {
			return "name";
		});

		dto.each("config", (item) -> {
			assertEquals("name", item.getString("name"));
		});

		assertEquals("李四", dto.getString("person.name"));
		assertEquals(15, dto.getInt("id"));
	}

	private final String _code0 = "{\"name\":\"名称\",\"orderIndex\":\"1\",\"markedCode\":\"markedCode\",\"description\":\"这是一项描述\",\"attached\":[{\"name\":\"配置1\",\"type\":\"3\",\"required\":\"true\",\"options\":\"选项1\"},{\"name\":\"配置2\",\"type\":\"2\",\"required\":\"true\",\"options\":\"选项1，选项2\"},{\"name\":\"配置3\",\"type\":\"4\",\"required\":\"false\",\"options\":\"选项1，选项2，选项3\"}]}";

	/// <summary>
	/// 变换的时候集合是空的
	/// </summary>
	@Test
	public void transformListIsEmpty() {
		var code = "{\"config\":[],\"description\":\"类型描述\",\"id\":13,\"markedCode\":\"test\",\"name\":\"测试\",\"orderIndex\":1}";
		DTObject dto = DTObject.editable(code);
		dto.transform("id=>typeId;config=>attached");
		dto.transform("attached.options=options", (v) -> {
			var options = ListUtil.map((Object[]) v, (t) -> {
				return (String) t;
			});

			return StringUtil.join(",", options);
		});
		assertEquals(
				"{\"attached\":[],\"description\":\"类型描述\",\"markedCode\":\"test\",\"name\":\"测试\",\"orderIndex\":1,\"typeId\":13}",
				dto.getCode(true));
	}

	private final String _code1 = "{\"config\":[{\"message\":\"\",\"name\":\"1\",\"options\":[\"选项1\",\"选项2\"],\"required\":true,\"type\":4,\"persons\":[{id:\"1\",name:\"姓名1\"},{id:\"2\",name:\"姓名2\"}]}],\"description\":\"111\",\"id\":7,\"markedCode\":\"1\",\"name\":\"123\",\"orderIndex\":1,\"rootId\":6}";

	/// <summary>
	/// 保留语句
	/// </summary>
	@Test
	public void reserve() {
		DTObject dto = DTObject.editable(_code1);
		dto.transform("~config.name,config.options,config.persons,description,id");
		assertEquals(
				"{\"config\":[{\"name\":\"1\",\"options\":[\"选项1\",\"选项2\"],\"persons\":[{\"id\":\"1\",\"name\":\"姓名1\"},{\"id\":\"2\",\"name\":\"姓名2\"}]}],\"description\":\"111\",\"id\":7}",
				dto.getCode());

		dto = DTObject.editable(_code1);
		dto.transform("~config.name,config.options,config.persons.id,description,id");
		assertEquals(
				"{\"config\":[{\"name\":\"1\",\"options\":[\"选项1\",\"选项2\"],\"persons\":[{\"id\":\"1\"},{\"id\":\"2\"}]}],\"description\":\"111\",\"id\":7}",
				dto.getCode());
	}

	/// <summary>
	/// 移除语句
	/// </summary>
	@Test
	public void remove() {
		DTObject dto = DTObject.editable(_code1);
		dto.transform("!config.name,config.options,config.persons,description,id");
		assertEquals(
				"{\"config\":[{\"message\":\"\",\"required\":true,\"type\":4}],\"markedCode\":\"1\",\"name\":\"123\",\"orderIndex\":1,\"rootId\":6}",
				dto.getCode(true));

		dto = DTObject.editable(_code1);
		dto.transform("!config.name,config.options,config.persons.id,description,id");
		assertEquals(
				"{\"config\":[{\"message\":\"\",\"persons\":[{\"name\":\"姓名1\"},{\"name\":\"姓名2\"}],\"required\":true,\"type\":4}],\"markedCode\":\"1\",\"name\":\"123\",\"orderIndex\":1,\"rootId\":6}",
				dto.getCode(true));
	}

	/// <summary>
	/// 设置自己
	/// </summary>
	@Test
	public void setSelf() {
		var dto = DTObject.editable();
		dto.setInt(2);
		assertEquals(2, dto.getInt());

		var newDTO = DTObject.editable("{id:3}");
		dto.replace(newDTO); // 该表达式表示设置自己
		assertEquals("{\"id\":3}", dto.getCode());

	}

	public static class ObjectTest {

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

		public void name(String name) {
			_name = name;
		}

		private int _index = 1;

		public int index() {
			return _index;
		}

		public void index(int index) {
			_index = index;
		}

		private byte _sex;

		public byte sex() {
			return _sex;
		}

		public void sex(byte value) {
			_sex = value;
		}

		public ObjectTest() {
		}

	}

	@Test
	public void saveToObject() {
		var dto = DTObject.editable();
		dto.setInt("id", 1);
		dto.setString("name", "李四");
		dto.setByte("sex", (byte) 1);

		var obj = new ObjectTest();

		dto.save(obj);

		assertEquals(1, obj.getId());
		assertEquals(1, obj.index()); // 由于dto里没有index的数据，所以不会赋值给obj
		assertEquals("李四", obj.name());
		assertEquals((byte) 1, obj.sex());
	}

//	/// <summary>
//	/// 映射的对象内含有DTO成员
//	/// </summary>
//	@Test
//	public void mapInnerDTO() {
//		var code = "{\"name\":\"类型名称\",\"orderIndex\":\"1\",\"markedCode\":\"markedCode\",\"description\":\"描述\",\"coverConfig\":[{\"name\":\"1\",\"width\":\"111\",\"height\":\"111\"}],\"dcSlimConfig\":{\"items\":[{\"name\":\"配置1\",\"type\":\"2\",\"required\":\"false\",\"options\":[\"选项1\",\"选项2\",\"选项3\"]},{\"name\":\"配置2\",\"type\":\"4\",\"required\":\"true\",\"options\":[\"选项1\",\"选项2\"]}]}}";
//		var para = DTObject.editable(code);
//
//		var temp = DTObject.deserialize < MapInnerDTOClass > (para);
//		Assert.AreEqual(
//				"\"dcSlimConfig\":{\"items\":[{\"name\":\"配置1\",\"options\":[\"选项1\",\"选项2\",\"选项3\"],\"required\":\"false\",\"type\":\"2\"},{\"name\":\"配置2\",\"options\":[\"选项1\",\"选项2\"],\"required\":\"true\",\"type\":\"4\"}]}",
//				temp.DCSlimConfig.GetCode(true));
//
//		var dto = DTObject.Serialize(temp);
//		var dcSlimConfig = dto.GetObject("dcSlimConfig");
//		Assert.AreEqual(
//				"\"dcSlimConfig\":{\"items\":[{\"name\":\"配置1\",\"options\":[\"选项1\",\"选项2\",\"选项3\"],\"required\":\"false\",\"type\":\"2\"},{\"name\":\"配置2\",\"options\":[\"选项1\",\"选项2\"],\"required\":\"true\",\"type\":\"4\"}]}",
//				temp.DCSlimConfig.GetCode(true));
//
//	}

	public class MapInnerDTOClass {

		private String _name;

		public String getName() {
			return _name;
		}

		public void setName(String name) {
			_name = name;
		}

		private short _orderIndex;

		public short getOrderIndex() {
			return _orderIndex;
		}

		public void setOrderIndex(short orderIndex) {
			_orderIndex = orderIndex;
		}

		private String _markedCode;

		public String getMarkedCode() {
			return _markedCode;
		}

		public void setMarkedCode(String markedCode) {
			_markedCode = markedCode;
		}

		private String _description;

		public String getDescription() {
			return _description;
		}

		public void setDescription(String description) {
			_description = description;
		}

		public DTObject _dscLimConfig;

		public void setDscLimConfig(DTObject data) {
			_dscLimConfig = data;
		}

		public DTObject getDscLimConfig() {
			return _dscLimConfig;
		}

	}
//
//	[TestMethod]
//
//	public void DynamicDTO()
//	{
//	    var dto = DTObject.Create("{name:\"张三丰\",sex:'男',person:{name:'张无忌',sex:'男'},persons:[{id:1,name:'1的名称'},{id:2,name:'2的名称'}], values:[1,2,3]}");
//	    dynamic d = (dynamic)dto;
//
//	    var name = d.Name;
//	    Assert.AreEqual("张三丰", name);
//
//	    var sex = d.GetValue<string>("sex");
//	    Assert.AreEqual("男", sex);
//
//	    var height = d.Height;
//	    Assert.IsNull(height);
//
//
//	    name = d.person.name;
//	    Assert.AreEqual("张无忌", name);
//
//
//	    Person person = d.person;
//	    Assert.AreEqual("张无忌", person.Name);
//
//	    var persons = d.persons;
//	    Assert.AreEqual("1的名称", persons[0].Name);
//	    Assert.AreEqual("2的名称", persons[1].Name);
//
//	    var values = d.values.OfType<int>();
//	    Assert.AreEqual(1, values[0]);
//
//	}
//
//	private class Person {
//		public string Name
//		{ get; set; }
//
//		public string Sex
//		{ get; set; }
//	}
//
//	[TestMethod]
//
//	public void MenuMapDTO() {
//		var menu = CreateMenu();
//
//		var dto = DTObject.Create("{name,index,parent:{name}}", menu);
//		var code = dto.GetCode(true);
//		Assert.AreEqual("{\"Index\":1,\"Name\":\"主菜单\",\"Parent\":{\"Name\":\"根菜单\"}}", code);
//
//		dto = DTObject.Create("{name,index,parent,owner}", menu);
//		code = dto.GetCode(true);
//		Assert.AreEqual(
//				"{\"Index\":1,\"Name\":\"主菜单\",\"Parent\":{\"Index\":0,\"Name\":\"根菜单\",\"Owner\":{\"Creator\":{\"Name\":\"创建人\",\"Sex\":\"男\"},\"Id\":\"project1\",\"Name\":\"项目1\"}}}",
//				code);
//
//	}
//
//	private Menu CreateMenu()
//	{
//	    var root = new Menu();
//	    root.Name = "根菜单";
//	    root.Index = 0;
//	    root.Childs = new List<Menu>();
//	    root.Owner = new Project() { Name = "项目1", Id = "project1" };
//	    root.Owner.Creator = new Person() { Name = "创建人", Sex = "男" };
//
//	    var menu = new Menu();
//	    menu.Name = "主菜单";
//	    menu.Index = 1;
//
//
//	    root.Childs.Add(menu);
//	    menu.Parent = root;
//
//	    menu.Childs = new List<Menu>();
//	    menu.Childs.Add(new Menu() { Name = "子菜单1", Index = 2 });
//
//	    menu.Childs.Add(new Menu() { Name = "子菜单2", Index = 3 });
//
//	    var childMenu3 = new Menu() { Name = "子菜单3", Index = 4 };
//	    childMenu3.Childs = new List<Menu>();
//	    childMenu3.Childs.Add(new Menu() { Name = "子菜单3-1", Index = 5 });
//
//	    childMenu3.Childs.Add(new Menu() { Name = "子菜单3-2", Index = 6 });
//	    menu.Childs.Add(childMenu3);
//
//	    return menu;
//	}
//
//	[TestMethod]
//
//	public void MenuListMapDTO()
//	{
//	    var menu = CreateMenu();
//
//	    var dto = DTObject.Create("{name,childs:[{name,index}]}", menu);
//	    var code = dto.GetCode(true);
//	    Assert.AreEqual("{\"Childs\":[{\"Index\":2,\"Name\":\"子菜单1\"},{\"Index\":3,\"Name\":\"子菜单2\"},{\"Index\":4,\"Name\":\"子菜单3\"}],\"Name\":\"主菜单\"}", code);
//
//	    dto = DTObject.Create("{name,childs}", menu);
//	    code = dto.GetCode(true);
//	    Assert.AreEqual("{\"Childs\":[{\"Childs\":[],\"Name\":\"子菜单1\"},{\"Childs\":[],\"Name\":\"子菜单2\"},{\"Childs\":[{\"Childs\":[],\"Name\":\"子菜单3-1\"},{\"Childs\":[],\"Name\":\"子菜单3-2\"}],\"Name\":\"子菜单3\"}],\"Name\":\"主菜单\"}", code);
//	}
//
//	[TestMethod]
//
//	public void MenuListMapObject()
//	{
//	    var code = "{\"Childs\":[{\"Childs\":[],\"Name\":\"子菜单1\"},{\"Childs\":[],\"Name\":\"子菜单2\"},{\"Childs\":[{\"Childs\":[],\"Name\":\"子菜单3-1\"},{\"Childs\":[],\"Name\":\"子菜单3-2\"}],\"Name\":\"子菜单3\"}],\"Name\":\"主菜单\"}";
//	    var dto = DTObject.Create(code);
//	    var menu = dto.Save<Menu>();
//	    Assert.AreEqual(3, menu.Childs.Count);
//	}
//
//	private class Menu {
//		public Menu Parent
//		{ get; set; }
//
//		public string Name
//		{ get; set; }
//
//		public int Index
//		{ get; set; }
//
//		public List<Menu> Childs
//		{ get; set; }
//
//		public Project Owner
//		{ get; set; }
//
//	}
//
//	private class Project {
//		public string Name
//		{ get; set; }
//
//		public string Id
//		{ get; set; }
//
//		public Person Creator
//		{ get; set; }
//
//	}
//
//	[TestMethod]
//
//	public void DTOEquals()
//	{
//	    var code1 = "{\"Name\":\"主菜单\",value:1}";
//	    var code2 = "{value:1,\"name\":\"主菜单\"}";
//	    var dto1 = DTObject.Create(code1);
//	    var dto2 = DTObject.Create(code2);
//	    Assert.AreEqual(dto1, dto2);
//	}
//
//	[TestMethod]
//
//	public void Multithreading()
//	{
//	    //to do...
//	}
//
//	[TestMethod]
//
//	public void MoreCode()
//	{
//	    var fileName = @"D:\Workspace\Projects\CodeArt Framework\CodeArt\CodeArtTest\DTO\code.txt";
//	    var sourceCode = System.IO.File.ReadAllText(fileName);
//	    {
//	        var dto = DTObject.Create(sourceCode);
//	        Assert.AreEqual(sourceCode, dto.GetCode(false,false));
//	    }
//
//	    Parallel.For(0, 100, (index) =>
//	    {
//	        var dto = DTObject.Create(sourceCode);
//	        Assert.AreEqual(sourceCode, dto.GetCode());
//	    });
//	}
//
//	[TestMethod]
//
//	public void QuotationMarks() {
//		var dto1 = DTObject.Create();
//		dto1["Code"] = "N'DTO',\"DTO\"";
//		var dto2 = DTObject.Create(dto1.GetCode());
//
//		Assert.AreEqual(dto1.GetCode(), dto2.GetCode());
//	}
}
