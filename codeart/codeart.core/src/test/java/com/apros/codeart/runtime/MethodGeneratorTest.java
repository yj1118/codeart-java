package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.apros.codeart.bytecode.ClassGenerator;
import com.apros.codeart.bytecode.LogicOperator;
import com.apros.codeart.core.TestRunner;

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
					mg.loadParameter("value");
					mg.load(1);
					return LogicOperator.AreEqual;
				}, () -> {
					mg.load(1);
					mg.exit();
				}, () -> {
					mg.load(0);
					mg.exit();
				});
				mg.load(5);
			}

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("get", int.class);
			var value = method.invoke(null, 1);

			assertEquals(1, value);

		} catch (Exception e) {
			throw propagate(e);
		}
	}

	@Test
	void each() {
		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic("print", void.class, (args) -> {
				args.add("list", List.class);
			})) {

				mg.each("list", String.class, (item) -> {
					mg.print(() -> {
						item.load();
					});
				});
			}

//			cg.save();

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("print", List.class);

			var temp = List.of("1", "2", "3");

			method.invoke(null, temp);

//			assertEquals(1, value);

		} catch (Exception e) {
			throw propagate(e);
		}
	}

	public class setFieldTestObject { // 方法内部的局部类

		private int _index;

		public int getIndex() {
			return _index;
		}

		public void setIndex(int value) {
			_index = value;
		}

		public setFieldTestObject(int index) {
			_index = index;
		}
	}

	@Test
	void setField() {

		try (var cg = ClassGenerator.define()) {

			final int index = 5;
			try (var mg = cg.defineMethodPublicStatic("setField", void.class, (args) -> {
				args.add("test", setFieldTestObject.class);
			})) {
				mg.assignField("test.index", () -> {
					mg.load(index);
				});
			}

//			cg.save();

			// 返回生成的字节码
			var cls = cg.toClass();

			var test = new setFieldTestObject(1);
			var method = cls.getDeclaredMethod("setField", setFieldTestObject.class);
			method.invoke(null, test);

			assertEquals(index, test.getIndex());

		} catch (Exception e) {
			throw propagate(e);
		}
	}

}
