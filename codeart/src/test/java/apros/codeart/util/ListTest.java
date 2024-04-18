package apros.codeart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

class ListTest {

	@Test
	void getEmpty() {
		var list = ListUtil.<Long>empty();

		assertEquals(0, Iterables.size(list));

		var list2 = ListUtil.<Integer>empty();
		assertEquals(0, Iterables.size(list2));

		var try_write = (List<Long>) list;
		boolean writeable = true;
		try {
			try_write.add(1L);
		} catch (UnsupportedOperationException e) {
			writeable = false;
		}
		assertEquals(false, writeable);

		assertEquals(0, Iterables.size(list));
		assertEquals(0, Iterables.size(list2));
	}

}
