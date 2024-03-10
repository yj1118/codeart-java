package com.apros.codeart.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

class SchemeTest {

	@Test
	public void Common() {
		DTObject schema = DTObject.readonly("{id}");

		var members = schema.getMembers();

		assertEquals(1, Iterables.size(members));

		var member = Iterables.get(members, 0);
		assertEquals("id", member.getName());
	}

}
