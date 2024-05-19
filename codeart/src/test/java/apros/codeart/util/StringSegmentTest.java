package apros.codeart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

//@ExtendWith(TestRunner.class)
class StringSegmentTest {

	@Test
	void trim() throws Exception {
		var sg = new StringSegment(" abc defg ");
		assertEquals("abc defg", sg.trim().toString());
		assertEquals("abc defg ", sg.trimStart().toString());
		assertEquals(" abc defg", sg.trimEnd().toString());
	}

	@Test
	void startsWith() throws Exception {
		var sg = new StringSegment("abcdefg");
		assertEquals(true, sg.startsWith('a'));
		assertEquals(true, sg.startsWith("a"));
		assertEquals(true, sg.startsWith("abc"));

		assertEquals(false, sg.startsWith("1a"));
	}

	@Test
	void endsWith() throws Exception {
		var sg = new StringSegment("abcdefg");
		assertEquals(true, sg.endsWith('g'));
		assertEquals(true, sg.endsWith("g"));
		assertEquals(true, sg.endsWith("efg"));
	}

	@Test
	void substr() throws Exception {
		var sg = new StringSegment("abcdefg");
		assertEquals("abc", sg.substr(0, 3).toString());
		assertEquals("bcd", sg.substr(1, 3).toString());
		assertEquals("defg", sg.substr(3).toString());
	}

	@Test
	void indexOf() throws Exception {
		var sg = new StringSegment("abcdefg");
		assertEquals(-1, sg.indexOf("F"));
		assertEquals(0, sg.indexOf("a"));
		assertEquals(1, sg.indexOf("bcd"));
		assertEquals(6, sg.indexOf("g"));
	}

}
