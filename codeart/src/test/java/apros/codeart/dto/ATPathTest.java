package apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ATPathTest {

	@Test
	public void common() {
		DTObject dto = DTObject.readonly("{\"mq.rpc\":{\"client.timeout\":20,group:\"test\"}}");

		var rpc = dto.getObject("@mq.rpc");

		var timeout = rpc.getInt("@client.timeout", 0);

		assertEquals(20, timeout);

	}

	@Test
	public void moreFlag() {
		DTObject dto = DTObject.readonly("{\"mq.rpc\":{\"client.timeout\":20,group:\"test\"}}");

		var group = dto.getString("@mq.rpc.@group");

		assertEquals("test", group);

		var timeout = dto.getInt("@mq.rpc.@client.timeout", 0);
		assertEquals(20, timeout);

	}

}
