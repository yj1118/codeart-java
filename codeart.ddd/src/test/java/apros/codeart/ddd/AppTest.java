package apros.codeart.ddd;

import static apros.codeart.i18n.Language.strings;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void shouldAnswerWithTrue() {
		assertTrue(true);
		var str = strings("apros.codeart.ddd", "DataTypeNotSupported", "xxx");
	}
}
