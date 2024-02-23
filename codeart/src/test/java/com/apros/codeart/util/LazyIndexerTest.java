package com.apros.codeart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.apros.codeart.TestRunner;

@ExtendWith(TestRunner.class)
class LazyIndexerTest {

	static class Data {

		private String _name;

		private int _id;

		public String getKey() {
			return String.format("%s_%s", _name, _id);
		}

		public Data(String name) {
			_id = increment();
			_name = name;
		}

		private static int _index = 0;

		private static synchronized int increment() {
			_index++;
			return _index;
		}

	}

	@Test
	void common() {
		var get = LazyIndexer.<String, Data>init((name) -> {
			return new Data(name);
		});

		var a1 = get.apply("a");

		assertEquals("a_1", a1.getKey());

		var a2 = get.apply("a");

		assertEquals("a_1", a2.getKey());

		// 由于b是新的名称，所以会新构建一个对象，并且序号+1，变为2
		var b1 = get.apply("b");

		assertEquals("b_2", b1.getKey());

		var b2 = get.apply("b");

		assertEquals("b_2", b2.getKey());
	}

}
