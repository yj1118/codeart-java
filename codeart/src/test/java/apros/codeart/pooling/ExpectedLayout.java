package apros.codeart.pooling;

import org.junit.jupiter.api.Assertions;

@SuppressWarnings({ "exports" })
class ExpectedLayout {

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

	public int borrowedCount() {
		return _matrixLayout.borrowedCount();
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

		public void setVectors(VectorLayout[] vectors) {
			_vectors = vectors;
		}

		public int borrowedCount() {
			int count = 0;
			for (var vector : _vectors) {
				count += vector.borrowedCount();
			}
			return count;
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

		public void inc(int capacity) {
			_dualIndex = (_dualIndex + 1) % 2;
			_storePointer = _capacity - 1; // 指向老容量的最大值-1，即：扩容后，下次取数据会+1,然后就会从增加的容量的第0号位开始借出对象
			_capacity = capacity;
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

	public static void assertLayout(ExpectedLayout info, Pool.Layout layout) {

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

	public static void assertLayout(ExpectedLayout.VectorLayout info, Pool.VectorLayout layout) {
		// 矢量池的容量
		Assertions.assertEquals(info.capacity(), layout.capacity());

		// 有效存储数组的索引
		Assertions.assertEquals(info.dualIndex(), layout.dualIndex());

		// 已借出的项
		Assertions.assertEquals(info.borrowedCount(), layout.borrowedCount());

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

}
