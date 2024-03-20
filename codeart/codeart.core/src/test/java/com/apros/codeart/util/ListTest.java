package com.apros.codeart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

class ListTest {

	@Test
	void getEmpty() {
		var list = ListUtil.<Long>empty();

		assertEquals(0, Iterables.size(list));

		var list2 = ListUtil.<Integer>empty();

		assertEquals(0, Iterables.size(list2));
	}

}
