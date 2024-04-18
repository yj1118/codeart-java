package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DTOPerformanceTest {

	@Test
	public void CreateDTOByPerformance() {
		DTObject dto = DTObject.editable("{id,name,childs:[{id,name}]}");

		dto.setInt("id", 1);
		dto.setString("name", "刘备");

		var child0 = dto.push("childs");
		child0.setInt("id", 2);
		child0.setString("name", "赵云");

		var child1 = dto.push("childs");
		child1.setInt("id", 3);
		child1.setString("name", "马超");

		// Assert.AreEqual("{\"id\",\"name\",\"childs\":[{\"id\",\"name\"}]}",
		// dto.GetCode(false));

		StringBuilder code = new StringBuilder();
		code.append("{\"childs\":[");
		code.append("{\"id\":2,\"name\":\"赵云\"},");
		code.append("{\"id\":3,\"name\":\"马超\"}");
		code.append("],\"id\":1,\"name\":\"刘备\"}");

		assertEquals(code.toString(), dto.getCode(true));

//		 var data = TimeMonitor.Oversee(() =>
//		 {
//		 for (var i = 0; i < 10000; i++)
//		 {
//		 DTObject.Create("{id,name,childs:[{id,name}]}");
//		 }
//		 });
//		 Assert.IsTrue(false, data.GetTime(0).ElapsedMilliseconds.ToString());
	}

	@Test
	public void CreateDTOBySimpleValue() {
		var dto = DTObject.readonly("{name:'刘备'}");
		assertEquals("刘备", dto.getString("name"));

//	    var data = TimeMonitor.Oversee(() =>
//	    {
//	        for (var i = 0; i < 10000; i++)
//	        {
//	            DTObject.Create("{name:'刘备',childs:[{id,name,sex}]}");
//	        }
//	    });
//	    Assert.IsTrue(false, data.GetTime(0).ElapsedMilliseconds.ToString());

	}
}
