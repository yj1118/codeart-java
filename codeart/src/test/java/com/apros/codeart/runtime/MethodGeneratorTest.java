package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.apros.codeart.TestRunner;

@ExtendWith(TestRunner.class)
class MethodGeneratorTest {

	public static class SampleClass {
		private String _name;

		public String getName() {
			return _name;
		}

		private int _index;

		public int getIndex() {
			return _index;
		}

		public SampleClass(String name, int index) {
			_name = name;
			_index = index;
		}
	}

	public static interface IOperater {

	}

	public static class Operater implements IOperater {

		public Operater() {

		}

		public void write(String value, int index) {
			System.out.println(value);
		}

	}

	private void invoke(SampleClass obj, IOperater operater) {

		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublic(true, "serialize", Void.class, (args) -> {
				args.add("obj", SampleClass.class);
				args.add("writer", Operater.class);
			})) {
				mg.invoke("writer.write", () -> {
					mg.load_field_value("obj._name");
					mg.load_field_value("obj._index");
				});
			}

//			cg.save();
//
//			// 返回生成的字节码
			var cls = cg.toClass();
//
//			var method = cls.getDeclaredMethod("serialize", SampleClass.class, Operater.class);
//			method.invoke(null, obj, operater);

		} catch (Exception e) {
			throw propagate(e);
		}

	}

	/**
	 * 
	 */
	@Test
	void common() {
		var obj = new SampleClass("小李", 1);
		var op = new Operater();
		invoke(obj, op);
	}

}
