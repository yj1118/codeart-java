package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.apros.codeart.TestRunner;
import com.apros.codeart.bytecode.ClassGenerator;
import com.apros.codeart.bytecode.LogicOperator;

@ExtendWith(TestRunner.class)
class MethodGeneratorTest {

	public static interface IOperater {
		void write(String value, int index);
	}

	public static class Operater implements IOperater {

		public Operater() {

		}

		public void write(String value, int index) {
			System.out.println(value);
			System.out.println(index);
		}

	}

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

	private void invoke(SampleClass obj, IOperater operater) {

		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic("serialize", void.class, (args) -> {
				args.add("obj", SampleClass.class);
				args.add("writer", Operater.class);
			})) {
				mg.invoke("writer.write", () -> {
					mg.invoke("obj.getName");
					mg.invoke("obj.getIndex");
				});
			}

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("serialize", SampleClass.class, Operater.class);
			method.invoke(null, obj, operater);

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

	@Test
	void if_else() {
		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic("get", int.class, (args) -> {
				args.add("value", int.class);
			})) {
				mg.when(() -> {
					mg.load_parameter("value");
					mg.load_const(1);
					return LogicOperator.AreEqual;
				}, () -> {
					mg.load_const(1);
					mg.exit();
				}, () -> {
					mg.load_const(0);
					mg.exit();
				});
				mg.load_const(5);
			}

//			cg.save();

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("get", int.class);
			var value = method.invoke(null, 1);

			assertEquals(1, value);

		} catch (Exception e) {
			throw propagate(e);
		}
	}

}
