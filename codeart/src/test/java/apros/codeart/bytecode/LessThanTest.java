package apros.codeart.bytecode;

import static apros.codeart.runtime.Util.propagate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class LessThanTest {

	public static class LessThanTestObject { // 方法内部的局部类

		public int intValue = 2;

		public long longValue = 2;

		public byte byteValue = 3;

		public short shortValue = 4;

		public float floatValue = 2.5F;

		public double doubleValue = 3.5;

		public LessThanTestObject() {
		}
	}

	private void lessThan(String valueName, Consumer<MethodGenerator> loadValue, boolean lessThan) {
		try (var cg = ClassGenerator.define()) {

			var obj = new LessThanTestObject();

			try (var mg = cg.defineMethodPublicStatic("test", int.class, (args) -> {
				args.add("obj", LessThanTestObject.class);
			})) {
				mg.when(() -> {
					mg.loadField("obj." + valueName);
					loadValue.accept(mg);
					return LogicOperator.LessThan;
				}, () -> {
					mg.load(1);
					mg.exit();
				}, () -> {
					mg.load(0);
					mg.exit();
				});
				mg.load(-1);
			}

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("test", LessThanTestObject.class);
			var value = method.invoke(null, obj);

			assertEquals(lessThan ? 1 : 0, value);

		} catch (Exception e) {
			throw propagate(e);
		}
	}

	@Test
	void byteLessThan() {
		lessThan("byteValue", (mg) -> {
			mg.load((byte) 5);
		}, true);

		lessThan("byteValue", (mg) -> {
			mg.load((byte) 1);
		}, false);
	}

	@Test
	void intLessThan() {
		lessThan("intValue", (mg) -> {
			mg.load(55);
		}, true);

		lessThan("intValue", (mg) -> {
			mg.load(1);
		}, false);
	}

	@Test
	void longLessThan() {
		lessThan("longValue", (mg) -> {
			mg.load(55L);
		}, true);

		lessThan("longValue", (mg) -> {
			mg.load(1L);
		}, false);
	}

	@Test
	void floatLessThan() {
		lessThan("floatValue", (mg) -> {
			mg.load(5F);
		}, true);

		lessThan("floatValue", (mg) -> {
			mg.load(1F);
		}, false);
	}

	@Test
	void doubleLessThan() {
		lessThan("doubleValue", (mg) -> {
			mg.load((double) 55);
		}, true);

		lessThan("doubleValue", (mg) -> {
			mg.load((double) 1);
		}, false);
	}

}
