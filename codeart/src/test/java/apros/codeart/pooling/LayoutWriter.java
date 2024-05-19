package apros.codeart.pooling;

import org.junit.jupiter.api.Assertions;

import apros.codeart.pooling.ExpectedLayout.VectorLayout;

@SuppressWarnings({ "exports" })
class LayoutWriter {

	private ExpectedLayout _data;

	public LayoutWriter(ExpectedLayout data) {
		_data = data;
	}

	/**
	 * 
	 * 横向扩容（向量扩容）
	 * 
	 * @param vectorCapacity 扩容后每个向量池的容量
	 */
	public void vectorInc(int vectorCapacity) {
		int totalCapacity = 0;
		for (var vector : _data.matrixLayout().vectors()) {
			vector.inc(vectorCapacity);
			totalCapacity += vectorCapacity;
		}
		_data.setTotalCapacity(totalCapacity);

		// 每次向量池扩容，必然是偏移段指针后，发现取不出数据，再扩容，
		// 所以这里也要同步模拟这个效果
		var vectorPointer = _data.vectorPointer();
		vectorPointer = (vectorPointer + 1) % _data.vectorCount();
		_data.setVectorPointer(vectorPointer);
	}

	/**
	 * 
	 * 矩阵池扩容
	 * 
	 * @param vectorCount 扩容后向量池的数量
	 */
	public void matrixInc(int vectorCount) {

		// 1.增加向量池
		var oldVectors = _data.matrixLayout().vectors();

		VectorLayout[] newVectors = new VectorLayout[vectorCount];

		// 拷贝老数据
		for (var i = 0; i < oldVectors.length; i++) {
			newVectors[i] = oldVectors[i];
		}

		// 建立新数据
		var vectorCapacity = oldVectors[0].capacity();
		for (var i = oldVectors.length; i < vectorCount; i++) {
			newVectors[i] = new VectorLayout(vectorCapacity, 0, 0, vectorCapacity);
		}

		_data.matrixLayout().setVectors(newVectors);

		int totalCapacity = vectorCapacity * vectorCount;
		_data.setTotalCapacity(totalCapacity);
		_data.setVectorCount(vectorCount);

		// 每次矩阵池扩容，会将指针偏移到扩容前的最后一项，这样就可以在下次借项时，用扩容后的第一个新项
		var vectorPointer = oldVectors.length - 1;
		_data.setVectorPointer(vectorPointer);

		_data.setMatrixDualIndex((_data.matrixDualIndex() + 1) % 2);

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

	/**
	 * 
	 * 推进一次向量池指针
	 * 
	 * @return
	 */
	public LayoutWriter nextVectorPointer() {
		var vp = _data.vectorPointer();
		var vc = _data.vectorCount();

		var index = (vp + 1) % vc;

		_data.setVectorPointer(index);
		return this;
	}

	public VectorWriter vector(int index) {
		var v = _data.matrixLayout().vectors()[index];
		var writer = new VectorWriter(_data, v, index);
		return writer;
	}

	public static class VectorWriter {

		private ExpectedLayout _matrixData;

		private ExpectedLayout.VectorLayout _data;

		private int _vectorIndex;

		public VectorWriter(ExpectedLayout matrixData, ExpectedLayout.VectorLayout data, int vectorIndex) {
			_matrixData = matrixData;
			_data = data;
			_vectorIndex = vectorIndex;
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
		public void borrowAfter() {
			_data.setBorrowedCount(_data.borrowedCount() + 1);
			_data.setWaitBorrowCount(_data.waitBorrowCount() - 1);
			_matrixData.setVectorPointer(_vectorIndex);
		}

		/**
		 * 本地线程从向量池中借出项，但是不影响全局向量池指针
		 */
		public void localBorrowAfter() {
			_data.setBorrowedCount(_data.borrowedCount() + 1);
			_data.setWaitBorrowCount(_data.waitBorrowCount() - 1);
		}

		/**
		 * 借出了一个临时项
		 */
		public void borrowTempAfter(IPoolItem item) {
			Assertions.assertTrue(Pool.isTemp(item));
		}

		public void backAfter() {
			_data.setBorrowedCount(_data.borrowedCount() - 1);
			_data.setWaitBorrowCount(_data.waitBorrowCount() + 1);
			_matrixData.setVectorPointer(_vectorIndex);
		}

	}

}