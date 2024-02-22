package test.codeart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import apros.codeart.context.ContextSession;
import apros.codeart.util.StringSegment;
import test.codeart.TestRunner;

@ExtendWith(TestRunner.class)
class StringSegmentTest {

	@Test
	void trim() throws Exception {
		ContextSession.using(() -> {
			var sg = StringSegment.obtain(" abc defg ");
			assertEquals("abc defg", sg.trim().toString());
			assertEquals("abc defg ", sg.trimStart().toString());
			assertEquals(" abc defg", sg.trimEnd().toString());
		});
	}

	@Test
	void startsWith() throws Exception {
		ContextSession.using(() -> {
			var sg = StringSegment.obtain("abcdefg");
			assertEquals(true, sg.startsWith('a'));
			assertEquals(true, sg.startsWith("a"));
			assertEquals(true, sg.startsWith("abc"));

			assertEquals(false, sg.startsWith("1a"));
		});
	}

}
