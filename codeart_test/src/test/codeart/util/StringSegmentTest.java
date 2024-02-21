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
	void startsWith() throws Exception {
		ContextSession.using(() -> {
			var sg = StringSegment.obtain("abcdefg");
			assertEquals(true, sg.startsWith('a'));
			assertEquals(true, sg.startsWith("a"));
			assertEquals(true, sg.startsWith("abc"));

		});
	}

}
