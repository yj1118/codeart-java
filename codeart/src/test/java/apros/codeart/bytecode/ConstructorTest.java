package apros.codeart.bytecode;

import static apros.codeart.runtime.Util.propagate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import apros.codeart.bytecode.ClassGenerator;

class ConstructorTest {

	public static class ConstructorTestObject { // 方法内部的局部类

		private String _name;

		public String getName() {
			return _name;
		}

		public ConstructorTestObject(String name) {
			_name = name;
		}

		public ConstructorTestObject() {
			_name = "";
		}
	}

	private void newObject(String name) {
		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic("test", ConstructorTestObject.class)) {
				mg.newObject(ConstructorTestObject.class, () -> {
					if (name != null)
						mg.load(name);
				});
			}

//			cg.save();

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("test");
			var obj = (ConstructorTestObject) method.invoke(null);

			if (name != null)
				assertEquals(name, obj.getName());
			else
				assertEquals("", obj.getName());

		} catch (Exception e) {
			throw propagate(e);
		}
	}

	@Test
	public void Arg0() {
		newObject(null);
	}

	@Test
	public void Arg1() {
		newObject("小李");
	}

	@Test
	public void arrayInt() {
		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic("test", int[].class)) {
				mg.newArray(int.class, 5);
			}

//			cg.save();

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("test");
			var array = (int[]) method.invoke(null);

			assertEquals(5, array.length);

		} catch (Exception e) {
			throw propagate(e);
		}
	}

	@Test
	public void arrayString() {
		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic("test", String[].class)) {
				mg.newArray(String.class, 3);
			}

//			cg.save();

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("test");
			var array = (String[]) method.invoke(null);

			assertEquals(3, array.length);

		} catch (Exception e) {
			throw propagate(e);
		}
	}

	@Test
	public void newList() {
		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic("getList", ArrayList.class)) {
				mg.newList();
			}

//			cg.save();

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("getList");
			var obj = method.invoke(null);

			var list = (ArrayList<Integer>) obj;

			list.add(1);
			assertEquals(1, list.get(0));
		} catch (Exception e) {
			throw propagate(e);
		}
	}

}
