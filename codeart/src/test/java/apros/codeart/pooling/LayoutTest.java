package apros.codeart.pooling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//@ExtendWith(TestRunner.class)
class LayoutTest {

	private static Pool<Object> _pool = new Pool<Object>(Object.class, new PoolConfig(10, 200, 60), (isTempItem) -> {
		return new Object();
	});

	// 因为默认会有2个矢量池（分段），由于设置的每个矢量池的初始值是10，所以一共有20个
	// 初始矩阵池的索引为0
	// 初始指向矢量池的下标为-1，表示还没有使用任何池
	// 初始矢量池的数量应该是2，这是框架内部设置好的
	// 在当前矩阵池中有2个向量池集合，一个是a，另外一个是b
	// 初始状态用的是第0个向量池集合，也就是a，a不为空，b没用到，所以为空
	private static final ExpectedLayout initialLayout = new ExpectedLayout(20, 0, -1, 2,
			new ExpectedLayout.VectorsLayout(false, true));

	/**
	 * 验证初始布局
	 */
	@Test
	void validateInitalLayout() {
		var layout = _pool.getLayout();
		var expected = initialLayout.copy();

		assertLayout(expected, layout);
	}

	private static class ExpectedLayout {

		private int _totalCapacity;

		/**
		 * 
		 * 池中的总容量
		 * 
		 * @return
		 */
		public int totalCapacity() {
			return _totalCapacity;
		}

		public void setTotalCapacity(int value) {
			_totalCapacity = value;
		}

		private int _matrixDualIndex;

		/**
		 * 
		 * 矩阵内使用的双池集合，当前用的是哪个索引的集合
		 * 
		 * @return
		 */
		public int matrixDualIndex() {
			return _matrixDualIndex;
		}

		public void setMatrixDualIndex(int value) {
			_matrixDualIndex = value;
		}

		private int _vectorPointer;

		/**
		 * 
		 * 当前指向矢量池的下标，也就是表示上次用的矢量池的位置
		 * 
		 * @return
		 */
		public int vectorPointer() {
			return _vectorPointer;
		}

		public void setVectorPointer(int value) {
			_vectorPointer = value;
		}

		private int _vectorCount;

		/**
		 * 
		 * 矢量池的数量
		 * 
		 * @return
		 */
		public int vectorCount() {
			return _vectorCount;
		}

		public void setVectorCount(int value) {
			_vectorCount = value;
		}

		private VectorsLayout _vectorsLayout;

		public VectorsLayout vectorsLayout() {
			return _vectorsLayout;
		}

		public ExpectedLayout(int totalCapacity, int matrixDualIndex, int vectorPointer, int vectorCount,
				VectorsLayout vectorsLayout) {
			_totalCapacity = totalCapacity;
			_matrixDualIndex = matrixDualIndex;
			_vectorPointer = vectorPointer;
			_vectorCount = vectorCount;
			_vectorsLayout = vectorsLayout;
		}

		public ExpectedLayout copy() {
			return new ExpectedLayout(this.totalCapacity(), this.matrixDualIndex(), this.vectorPointer(),
					this.vectorCount(), this.vectorsLayout().copy());
		}

		public static class VectorsLayout {

			private boolean _a_is_null;

			public boolean a_is_null() {
				return _a_is_null;
			}

			private boolean _b_is_null;

			public boolean b_is_null() {
				return _b_is_null;
			}

			public VectorsLayout(boolean a_is_null, boolean b_is_null) {
				_a_is_null = a_is_null;
				_b_is_null = b_is_null;
			}

			public VectorsLayout copy() {
				return new VectorsLayout(_a_is_null, _b_is_null);
			}

		}

	}

	private void assertLayout(ExpectedLayout info, Pool.Layout layout) {

		Assertions.assertEquals(info.totalCapacity(), layout.capacity());

		Assertions.assertEquals(info.matrixDualIndex(), layout.dualIndex());

		Assertions.assertEquals(info.vectorPointer(), layout.pointer());

		Assertions.assertEquals(info.vectorCount(), layout.vectorCount());

		if (info.vectorsLayout().a_is_null()) {
			Assertions.assertNull(layout.vectorsA());
		} else {
			Assertions.assertNotNull(layout.vectorsA());
		}

		if (info.vectorsLayout().b_is_null()) {
			Assertions.assertNull(layout.vectorsB());
		} else {
			Assertions.assertNotNull(layout.vectorsB());
		}

		var vectors = layout.vectorsA() != null ? layout.vectorsA() : layout.vectorsB();

		// 矢量池集合的大小，应该与池中记录的大小一致
		Assertions.assertEquals(vectors.length, layout.vectorCount());

	}

}
