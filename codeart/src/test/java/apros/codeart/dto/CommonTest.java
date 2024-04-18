package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

import apros.codeart.util.ISO8601;
import apros.codeart.util.StringUtil;

class CommonTest {

	@Test
	public void createDTO() {
		DTObject dto = DTObject.editable("{id,name}");
		dto.setInt("id", 1);
		dto.setString("name", "刘备");

		assertEquals(1, dto.getInt("id"));
		assertEquals("刘备", dto.getString("name"));
		assertEquals("{\"id\":1,\"name\":\"刘备\"}", dto.getCode());
		assertEquals("{id,name}", dto.getSchemaCode());
	}

	@Test
	public void createHaveValueDTO() {
		DTObject dto = DTObject.readonly("{id:1,name:\"Louis\"}");

		assertEquals(1, dto.getInt("id"));
		assertEquals("Louis", dto.getString("name"));
		assertEquals("{\"id\":1,\"name\":\"Louis\"}", dto.getCode());
		assertEquals("{id,name}", dto.getSchemaCode());
	}

	@Test
	public void createListDTO() {
		var dto = DTObject.editable("{id,name,hobby:[{v,n}]}");
		dto.setInt("id", 1);
		dto.setString("name", "Louis");
		DTObject obj = dto.push("hobby");
		obj.setInt("v", 0);
		obj.setString("n", String.format("LS%s", 0));

		obj = dto.push("hobby");
		obj.setInt("v", 1);
		obj.setString("n", String.format("LS%s", 1));

		var list = dto.getList("hobby");
		for (int i = 0; i < Iterables.size(list); i++) {
			var t = Iterables.get(list, i);
			assertEquals(i, t.getInt("v"));
			assertEquals(String.format("LS%s", i), t.getString("n"));
		}

		assertEquals(1, dto.getInt("id"));
		assertEquals("Louis", dto.getString("name"));
		assertEquals("{\"id\":1,\"name\":\"Louis\",\"hobby\":[{\"v\":0,\"n\":\"LS0\"},{\"v\":1,\"n\":\"LS1\"}]}",
				dto.getCode());

		var code = dto.getCode();
		var copy = DTObject.readonly(code);
		list = dto.getList("hobby");

		for (int i = 0; i < list.size(); i++) {
			var item = list.get(i);
			assertEquals(i, item.getInt("v"));
			assertEquals(String.format("LS%s", i), item.getString("n"));
		}

		assertEquals(1, dto.getInt("id"));
		assertEquals("Louis", dto.getString("name"));

	}

	@Test
	public void createNestListDTO() {
		DTObject dto = DTObject.editable("{items:[{v,n,childs:[{v,n}]}]}");

		DTObject item = dto.push("items");
		item.setInt("v", 0);
		item.setString("n", String.format("item%s", 0));

		item = dto.push("items");
		item.setInt("v", 1);
		item.setString("n", String.format("item%s", 1));

		DTObject itemChild = item.push("childs");
		itemChild.setInt("v", 10);
		itemChild.setString("n", String.format("child%s", 10));

		itemChild = item.push("childs");
		itemChild.setInt("v", 20);
		itemChild.setString("n", String.format("child%s", 20));

		assertEquals(
				"{\"items\":[{\"v\":0,\"n\":\"item0\",\"childs\":[]},{\"v\":1,\"n\":\"item1\",\"childs\":[{\"v\":10,\"n\":\"child10\"},{\"v\":20,\"n\":\"child20\"}]}]}",
				dto.getCode());

		assertEquals("{items:[{v,n,childs:[{v,n}]}]}", dto.getSchemaCode());
	}

	@Test
	public void createSymbolDTO() {
		DTObject dto = DTObject.editable("{id,name,sex,hobbys:[{v,n}]}");
		dto.setInt("id", 1);
		dto.setString("name", "loui's");
		dto.setInt("sex", 9);

		DTObject objHobbys = dto.push("hobbys");
		objHobbys.setString("v", "1");
		objHobbys.setString("n", "！@#09/");

		assertEquals(1, dto.getInt("id"));
		assertEquals("loui's", dto.getString("name"));
		assertEquals(9, dto.getInt("sex"));
		// Assert.AreEqual("{\"id\",\"name\",\"sex\",\"hobbys\":[{\"v\",\"n\"}]}",
		// dto.GetCode(false));
		assertEquals("{\"id\":1,\"name\":\"loui's\",\"sex\":9,\"hobbys\":[{\"v\":\"1\",\"n\":\"！@#09/\"}]}",
				dto.getCode());
	}

	@Test
	public void createStringDTO() {
		DTObject dto = DTObject.editable("{name}");
		dto.setString("name", StringUtil.empty());

		assertEquals(StringUtil.empty(), dto.getString("name"));
	}

	@Test
	public void createBoolDTO() {
		DTObject dto = DTObject.editable("{isShow}");
		dto.setBoolean("isShow", true);

		assertEquals(true, dto.getBoolean("isShow"));
	}

	@Test
	public void createLocalDateTimeDTO() {

		{
			var code = "{id:1,time:\"2024-03-06T15:45:30.123Z\"}";
			DTObject dto = DTObject.readonly(code);

			var time = dto.getLocalDateTime("time");

			assertEquals(2024, time.getYear());
			assertEquals(3, time.getMonthValue());
			assertEquals(6, time.getDayOfMonth());

			assertEquals(15, time.getHour());
			assertEquals(45, time.getMinute());
			assertEquals(30, time.getSecond());

			assertEquals("{\"id\":1,\"time\":\"2024-03-06T15:45:30.123Z\"}", dto.getCode());
		}

		{
			DTObject dto = DTObject.editable();

			dto.setInt("id", 1);

			var time = LocalDateTime.of(2024, 3, 6, 15, 45, 30);

			dto.setLocalDateTime("time", time);

			time = dto.getLocalDateTime("time");

			assertEquals(2024, time.getYear());
			assertEquals(3, time.getMonthValue());
			assertEquals(6, time.getDayOfMonth());

			assertEquals(15, time.getHour());
			assertEquals(45, time.getMinute());
			assertEquals(30, time.getSecond());

			// 本地时间在传输时，会追加时区信息
			var offset = ISO8601.getSystemZoneOffset();
			assertEquals(String.format("{\"id\":1,\"time\":\"2024-03-06T15:45:30%s\"}", offset), dto.getCode());
		}

	}

	@Test
	public void createSocketMessageDTO() {
		DTObject dto = DTObject.readonly(
				"{\"RCN\":\"ControlBigScreenCapability\",\"REN\":\"PlayEvent\",\"MT\":7,\"Ds\":[\"[::ffff:192.168.0.13]:59714\"]}");
		var ds = dto.getList("Ds");

		assertEquals(1, ds.size());

		assertEquals("[::ffff:192.168.0.13]:59714", ds.get(0).getString());
	}

}
