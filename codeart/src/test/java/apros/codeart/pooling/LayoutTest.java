package apros.codeart.pooling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//@ExtendWith(TestRunner.class)
class LayoutTest {

	private static Pool<Object> _pool;

	public static ExpectedLayout resetPool() {
		if (_pool != null)
			_pool.dispose();

		_pool = new Pool<Object>(Object.class, new PoolConfig(4, 200, 60), (isTempItem) -> {
			return new Object();
		});

		return initialLayout.copy();
	}

	// 因为默认会有2个向量池，由于设置的每个矢量池的初始值是10，所以一共有20个
	// 初始矩阵池的索引为0
	// 初始指向矢量池的下标为-1，表示还没有使用任何池
	// 初始矢量池的数量应该是2，这是框架内部设置好的
	// 有2个矩阵池（矢量池集合），一个是a，另外一个是b
	// 初始状态用的是第0个矩阵池，也就是a，a有效，b无效，所以为空
	private static final ExpectedLayout initialLayout = new ExpectedLayout(20, 0, -1, 2,
			new ExpectedLayout.MatrixLayout(0, new ExpectedLayout.VectorLayout[] {
					new ExpectedLayout.VectorLayout(10, 0, 0, -1), new ExpectedLayout.VectorLayout(10, 0, 0, -1) }));

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
			_matrixLayout.setMatrixDualIndex(value);
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

		private MatrixLayout _matrixLayout;

		public MatrixLayout matrixLayout() {
			return _matrixLayout;
		}

		public ExpectedLayout(int totalCapacity, int matrixDualIndex, int vectorPointer, int vectorCount,
				MatrixLayout matrixLayout) {
			_totalCapacity = totalCapacity;
			_matrixDualIndex = matrixDualIndex;
			_vectorPointer = vectorPointer;
			_vectorCount = vectorCount;
			_matrixLayout = matrixLayout;
		}

		public ExpectedLayout copy() {
			return new ExpectedLayout(this.totalCapacity(), this.matrixDualIndex(), this.vectorPointer(),
					this.vectorCount(), this.matrixLayout().copy());
		}

		public static class MatrixLayout {

			private int _matrixDualIndex;

			public boolean a_enable() {
				return _matrixDualIndex == 0;
			}

			public boolean b_enable() {
				return _matrixDualIndex == 1;
			}

			public void setMatrixDualIndex(int value) {
				_matrixDualIndex = value;
			}

			private VectorLayout[] _vectors;

			public VectorLayout[] vectors() {
				return _vectors;
			}

			public MatrixLayout(int matrixDualIndex, VectorLayout[] vectors) {
				_matrixDualIndex = matrixDualIndex;
				_vectors = vectors;
			}

			public MatrixLayout copy() {

				var vs = new VectorLayout[_vectors.length];

				for (var i = 0; i < vs.length; i++) {
					vs[i] = _vectors[i].copy();
				}

				return new MatrixLayout(_matrixDualIndex, vs);
			}

		}

		/**
		 * 矢量池的布局
		 */
		public static class VectorLayout {

			public boolean a_enable() {
				return _dualIndex == 0;
			}

			public boolean b_enable() {
				return _dualIndex == 1;
			}

			private int _capacity;

			/**
			 * 
			 * 池的容量
			 * 
			 * @return
			 */
			public int capacity() {
				return _capacity;
			}

			public void setCapacity(int value) {
				_capacity = value;
			}

			private int _dualIndex;

			/**
			 * 
			 * 向量池内使用的双存储数组，当前用的是哪个索引的存储数组
			 * 
			 * @return
			 */
			public int dualIndex() {
				return _dualIndex;
			}

			public void setDualIndex(int value) {
				_dualIndex = value;
			}

			private int _borrowedCount;

			/**
			 * 
			 * 已借出的数量
			 * 
			 * @return
			 */
			public int borrowedCount() {
				return _borrowedCount;
			}

			/**
			 * 
			 * 借出几项
			 * 
			 * @param value
			 */
			public void setBorrowedCount(int value) {
				_borrowedCount = value;
			}

			private int _storePointer;

			/**
			 * 
			 * 上一次借出去的项的索引（存储数据里的坐标）
			 * 
			 * @return
			 */
			public int storePointer() {
				return _storePointer;
			}

			public void setStorePointer(int value) {
				_storePointer = value;
			}

			public VectorLayout(int capacity, int dualIndex, int borrowedCount, int storePointer) {
				_capacity = capacity;
				_dualIndex = dualIndex;
				_borrowedCount = borrowedCount;
				_storePointer = storePointer;
			}

			public VectorLayout copy() {
				return new VectorLayout(_capacity, _dualIndex, _borrowedCount, _storePointer);
			}

		}

	}

	private void assertLayout(ExpectedLayout info, Pool.Layout layout) {

		// 验证总共可以提供缓存服务的对象数量
		Assertions.assertEquals(info.totalCapacity(), layout.capacity());

		// 验证使用的是哪个矩阵池，对应的下标是多少
		Assertions.assertEquals(info.matrixDualIndex(), layout.matrixDualIndex());

		Assertions.assertEquals(info.vectorPointer(), layout.vectorPointer());

		Assertions.assertEquals(info.vectorCount(), layout.vectorCount());

		if (info.matrixLayout().a_enable()) {
			Assertions.assertNotNull(layout.matrixA());
		} else {
			Assertions.assertNull(layout.matrixA());
		}

		if (info.matrixLayout().b_enable()) {
			Assertions.assertNotNull(layout.matrixB());
		} else {
			Assertions.assertNull(layout.matrixB());
		}

		var vectors = layout.matrixA() != null ? layout.matrixA() : layout.matrixB();

		// 矩阵池的大小，应该与池布局中记录的大小一致
		Assertions.assertEquals(vectors.length, layout.vectorCount());

		// 验证每个矢量池的布局
		for (var i = 0; i < vectors.length; i++) {
			assertLayout(info.matrixLayout().vectors()[i], vectors[i]);
		}

	}

	private void assertLayout(ExpectedLayout.VectorLayout info, Pool.VectorLayout layout) {
		// 矢量池的容量
		Assertions.assertEquals(info.capacity(), layout.capacity());

		// 有效存储数组的索引
		Assertions.assertEquals(info.dualIndex(), layout.dualIndex());

		// 已借出的项
		Assertions.assertEquals(info.borrowedCount(), layout.borrowedCount());

		//
		Assertions.assertEquals(info.storePointer(), layout.pointer());

		if (info.a_enable()) {
			Assertions.assertNotNull(layout.storeA());
		} else {
			Assertions.assertNull(layout.storeA());
		}

		if (info.b_enable()) {
			Assertions.assertNotNull(layout.storeB());
		} else {
			Assertions.assertNull(layout.storeB());
		}

		var store = layout.storeA() != null ? layout.storeA() : layout.storeB();

		// 向量池的容量就是存储数组的容量
		Assertions.assertEquals(info.capacity(), store.capacity());

		// 向量池的借出去的项数量就是存储数组的借出数量
		Assertions.assertEquals(info.borrowedCount(), store.borrowedCount());

	}

	private static class LayoutWriter {

		private ExpectedLayout _data;

		public LayoutWriter(ExpectedLayout data) {
			_data = data;
		}

		public LayoutWriter matrixA() {
			if (_data.matrixLayout().a_enable())
				return this;

			throw new IllegalStateException("matrixA未启用");
		}

		public LayoutWriter matrixB() {
			if (_data.matrixLayout().b_enable())
				return this;

			throw new IllegalStateException("matrixB未启用");
		}

		public VectorWriter vector(int index) {
			var v = _data.matrixLayout().vectors()[index];
			var writer = new VectorWriter(_data, v);
			return writer;
		}

		public static class VectorWriter {

			private ExpectedLayout _matrixData;

			private ExpectedLayout.VectorLayout _data;

			public VectorWriter(ExpectedLayout matrixData, ExpectedLayout.VectorLayout data) {
				_matrixData = matrixData;
				_data = data;
			}

			public VectorWriter storeA() {
				if (_data.a_enable())
					return this;

				throw new IllegalStateException("storeA未启用");
			}

			public VectorWriter storeB() {
				if (_data.b_enable())
					return this;

				throw new IllegalStateException("storeB未启用");
			}

			/**
			 * 
			 * 
			 * 
			 * @param count           借出多少项
			 * @param storePointer    借出项后，数值的变化
			 * @param vectorPointer   借出项后，数值的变化
			 * @param matrixDualIndex 借出项后，数值的变化
			 * @param vectorDualIndex 借出项后，数值的变化
			 */
			public void borrow(int count, int storePointer, int vectorPointer, int matrixDualIndex,
					int vectorDualIndex) {
				_data.setBorrowedCount(_data.borrowedCount() + count);
				_data.setStorePointer(storePointer);
				_matrixData.setVectorPointer(vectorPointer);
				_matrixData.setMatrixDualIndex(matrixDualIndex);
				_data.setDualIndex(vectorDualIndex);
			}

			public void back(int count, int storePointer, int vectorPointer, int matrixDualIndex, int vectorDualIndex) {
				_data.setBorrowedCount(_data.borrowedCount() - count);
				_data.setStorePointer(storePointer);
				_matrixData.setVectorPointer(vectorPointer);
				_matrixData.setMatrixDualIndex(matrixDualIndex);
				_data.setDualIndex(vectorDualIndex);
			}

		}

	}

	/**
	 * 验证初始布局
	 */
	@Test
	void initalLayout() {
		resetPool();
		var expected = resetPool();
		assertLayout(expected, _pool.getLayout());
	}

	@Test
	void borrowed1() {
		var expected = resetPool();
		var writer = new LayoutWriter(expected);

		// 借出一个
		var item = _pool.borrow();

		writer.matrixA().vector(0).storeA().borrow(1, 0, 0, 0, 0);

		assertLayout(expected, _pool.getLayout());

		item.back();

		// 归还后，不会影响布局
		writer.matrixA().vector(0).storeA().back(1, 0, 0, 0, 0);

		assertLayout(expected, _pool.getLayout());
	}

	/**
	 * 
	 */
	@Test
	void borrowed2() {
		var expected = resetPool();
		var writer = new LayoutWriter(expected);

		// 借2个
		var item0 = _pool.borrow();

		// 先从第0个向量池取第0个数据
		writer.matrixA().vector(0).storeA().borrow(1, 0, 0, 0, 0);

		var item1 = _pool.borrow();

		// 再从第1个向量池取第0个数据
		writer.matrixA().vector(1).storeA().borrow(1, 0, 1, 0, 0);

		var layout = _pool.getLayout();
		assertLayout(expected, layout);
	}

}
