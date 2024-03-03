package com.apros.codeart.bytecode;

import static com.apros.codeart.runtime.Util.propagate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.apros.codeart.TestRunner;

@ExtendWith(TestRunner.class)
class ForTest {

	public static class LoopTestItem { // 方法内部的局部类

		private int _value = 0;

		public int getValue() {
			return _value;
		}

		public void setValue(int v) {
			_value = v;
		}

		public LoopTestItem() {
		}
	}

	@Test
	void RefItemLoop() {
		try (var cg = ClassGenerator.define()) {

			ArrayList<LoopTestItem> obj = new ArrayList<>();

			for (var i = 0; i < 5; i++)
				obj.add(new LoopTestItem());

			try (var mg = cg.defineMethodPublicStatic("test", void.class, (args) -> {
				args.add("list", obj.getClass());
			})) {
				mg.loop(() -> {
					mg.loadParameter(0);
				}, (item, i, length) -> {
					var t = item.cast(LoopTestItem.class);
					mg.assignField(t, "value", () -> {
						mg.load(2);
					});
				});
			}

			cg.save();

			// 返回生成的字节码
			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("test", obj.getClass());
			method.invoke(null, obj);

			for (var t : obj) {
				assertEquals(2, t.getValue());
			}

		} catch (Exception e) {
			throw propagate(e);
		}
	}

}