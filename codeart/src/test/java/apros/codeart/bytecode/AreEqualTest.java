package apros.codeart.bytecode;

import static apros.codeart.runtime.Util.propagate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

//@ExtendWith(TestRunner.class)
class AreEqualTest {

	public static class AreEqualTestObject { // 方法内部的局部类

		public int intValue = 1;

		public long longValue = 2;

		public byte byteValue = 3;

		public short shortValue = 4;

		public float floatValue = 2.5F;

		public double doubleValue = 3.5;

		public AreEqualTestObject() {
		}
	}

	private void areEqual(String valueName, BiConsumer<MethodGenerator, AreEqualTestObject> loadValue) {
		try (var cg = ClassGenerator.define()) {

			var obj = new AreEqualTestObject();

			try (var mg = cg.defineMethodPublicStatic("test", int.class, (args) -> {
				args.add("obj", AreEqualTestObject.class);
			})) {
				mg.when(() -> {
					mg.loadField("obj." + valueName);
					loadValue.accept(mg, obj);
					return LogicOperator.AreEqual;
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

			var method = cls.getDeclaredMethod("test", AreEqualTestObject.class);
			var value = method.invoke(null, obj);

			assertEquals(1, value);

		} catch (Exception e) {
			throw propagate(e);
		}
	}

	@Test
	void byteAreEqual() {
		areEqual("byteValue", (mg, obj) -> {
			mg.load(obj.byteValue);
		});
	}

	@Test
	void longAreEqual() {
		areEqual("longValue", (mg, obj) -> {
			mg.load(obj.longValue);
		});
	}

	@Test
	void floatAreEqual() {
		areEqual("floatValue", (mg, obj) -> {
			mg.load(obj.floatValue);
		});
	}

	@Test
	void doubleAreEqual() {
		areEqual("doubleValue", (mg, obj) -> {
			mg.load(obj.doubleValue);
		});
	}

}
