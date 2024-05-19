package apros.codeart.pooling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//@ExtendWith(TestRunner.class)
@SuppressWarnings({ "unused" })
class SingleThreadedTest {

	private static Pool<Object> _pool;

	public static ExpectedLayout resetPool() {
		if (_pool != null)
			_pool.dispose();

		_pool = new Pool<Object>(Object.class, new PoolConfig(4, 8, 60), (isTempItem) -> {
			return new Object();
		});

		return initialLayout.copy();
	}

	// 因为默认会有2个向量池，由于设置的每个矢量池的初始值是4，所以一共有8个
	// 初始矩阵池的索引为0
	// 初始指向矢量池的下标为-1，表示还没有使用任何池
	// 初始矢量池的数量应该是2，这是框架内部设置好的
	// 有2个矩阵池（矢量池集合），一个是a，另外一个是b
	// 初始状态用的是第0个矩阵池，也就是a，a有效，b无效，所以为空
	private static final ExpectedLayout initialLayout = new ExpectedLayout(8, 0, -1, 2,
			new ExpectedLayout.MatrixLayout(0, new ExpectedLayout.VectorLayout[] {
					new ExpectedLayout.VectorLayout(4, 0, 0, 4), new ExpectedLayout.VectorLayout(4, 0, 0, 4) }));

	/**
	 * 验证初始布局
	 */
	@Test
	void initalLayout() {
		resetPool();
		var expected = resetPool();
		ExpectedLayout.assertLayout(expected, _pool.getLayout());
	}

	@Test
	void borrowed1() {
		var expected = resetPool();
		var writer = new LayoutWriter(expected);

		// 借出一个
		var item = _pool.borrow();

		writer.matrixA().vector(0).borrowAfter();

		ExpectedLayout.assertLayout(expected, _pool.getLayout());

		item.back();

		// 归还后，会影响布局
		writer.matrixA().vector(0).backAfter();

		ExpectedLayout.assertLayout(expected, _pool.getLayout());
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
		writer.matrixA().vector(0).borrowAfter();

		var item1 = _pool.borrow();

		// 再从第1个向量池取第0个数据
		writer.matrixA().vector(1).borrowAfter();

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(expected, layout);
	}

	@Test
	void borrowed3() {
		var expected = resetPool();
		var writer = new LayoutWriter(expected);

		// 借3个
		var item0 = _pool.borrow();

		// 先从第0个向量池取第0个数据
		writer.matrixA().vector(0).borrowAfter();

		var item1 = _pool.borrow();

		// 再从第1个向量池取第0个数据
		writer.matrixA().vector(1).borrowAfter();

		var item2 = _pool.borrow();

		// 再从第0个向量池取第1个数据
		writer.matrixA().vector(0).borrowAfter();

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(expected, layout);
	}

	/**
	 * 借出刚满池的数量
	 */
	@Test
	void borrowedJustFull() {
		var expected = resetPool();
		var writer = new LayoutWriter(expected);
		borrowed8(writer);

		Assertions.assertEquals(expected.borrowedCount(), 8);

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(expected, layout);
	}

	private void borrowed8(LayoutWriter writer) {
		// 借8个
		var item0 = _pool.borrow();

		// 先从第0个向量池取第0个数据
		writer.matrixA().vector(0).borrowAfter();

		var item1 = _pool.borrow();

		// 再从第1个向量池取第0个数据
		writer.matrixA().vector(1).borrowAfter();

		var item2 = _pool.borrow();

		// 再从第0个向量池取第1个数据
		writer.matrixA().vector(0).borrowAfter();

		var item3 = _pool.borrow();

		// 再从第1个向量池取第1个数据
		writer.matrixA().vector(1).borrowAfter();

		var item4 = _pool.borrow();

		// 再从第0个向量池取第2个数据
		writer.matrixA().vector(0).borrowAfter();

		var item5 = _pool.borrow();

		// 再从第1个向量池取第2个数据
		writer.matrixA().vector(1).borrowAfter();

		var item6 = _pool.borrow();

		// 再从第0个向量池取第3个数据
		writer.matrixA().vector(0).borrowAfter();

		var item7 = _pool.borrow();

		// 再从第1个向量池取第3个数据
		writer.matrixA().vector(1).borrowAfter();

	}

	/**
	 * 借超出1个
	 */
	@Test
	void borrowedMoreThan1() {
		var expected = resetPool();
		var writer = new LayoutWriter(expected);
		borrowed8(writer);

		var item0 = _pool.borrow();

		writer.vectorInc(6);

		// 多借出1个后，就扩容了
		writer.matrixA().vector(0).borrowTempAfter(item0);

		// 临时项不算在借出的项的数量里，严格上讲，它不属于池的成员
		Assertions.assertEquals(8, expected.borrowedCount());
		// 由于在借的时候，指针会++，所以又回到0了，然后再扩容
		Assertions.assertEquals(0, expected.vectorPointer());

		// 再借出超出1个的时候，实际上这个1个是临时项，池这时候被扩容了，
		// 下次再借的时候，取的就是新池的第0个数据

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(expected, layout);
	}

	/**
	 * 借超出2个
	 */

	@Test
	void borrowedMoreThan2() {
		var expected = resetPool();
		var writer = new LayoutWriter(expected);
		// 先借8个，这时候就池就满了
		borrowed8(writer);

		// 再借1个就会扩容
		var item0 = _pool.borrow();

		// 由于向量池还没有达到设置的最大上限，所以这次扩容是向量池的扩容，而不是矩阵池扩容
		// 向量池的存储量由4个扩容到6个
		writer.vectorInc(6);

		// 注意：由于本次借项是超出了，所以实际上借出的是临时项
		var item1 = _pool.borrow();

		// 4是新扩容的池里，第0个数据的下标
		// 扩容时会由存储A切换到存储B
		writer.matrixA().vector(1).borrowAfter();

		Assertions.assertEquals(9, expected.borrowedCount());
		// 由于在借的时候，指针会++，所以又回到0了，然后再扩容
		// 再借后，就为1了
		Assertions.assertEquals(1, expected.vectorPointer());

		// 再借出超出1个的时候，实际上这个1个是临时项，池这时候被扩容了，
		// 下次再借的时候，取的就是新池的第0个数据

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(expected, layout);
	}

	/**
	 * 
	 * 借9个刚好就是扩容了一次,第9个借出的是临时项，不影响池布局
	 * 
	 * @param writer
	 */
	private void borrowed9(LayoutWriter writer) {
		borrowed8(writer);
		_pool.borrow();
		writer.vectorInc(6);
	}

	private void borrowed13(LayoutWriter writer) {
		borrowed9(writer);
		_pool.borrow();

		writer.matrixA().vector(1).borrowAfter();

		_pool.borrow();

		writer.matrixA().vector(0).borrowAfter();

		_pool.borrow();

		writer.matrixA().vector(1).borrowAfter();

		_pool.borrow();

		writer.matrixA().vector(0).borrowAfter();

	}

	/**
	 * 借出刚好满第二次扩容（但是尚未扩容）
	 */
	@Test
	void borrowedJustFullAgain() {
		var expected = resetPool();
		var writer = new LayoutWriter(expected);

		borrowed13(writer);

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(expected, layout);
	}

	/**
	 * 第二次扩容（向量扩容）
	 */
	@Test
	void borrowedSecondInc() {
		var info = resetPool();
		var writer = new LayoutWriter(info);

		borrowed13(writer);

		// 第14个是临时项
		var item0 = _pool.borrow();

		// 本来是6扩容为9的，但是设置了上限8，所以就为8了
		writer.vectorInc(8);

		// 多借出1个后，就扩容了，导致切换到storeA了
		// 注意，由于运行一次，向量池下标就被推进了，所以跟上次比起来，由0变为1
		writer.matrixA().vector(1).borrowTempAfter(item0);

		// 第15个是扩容后的池的第一个
		_pool.borrow();
		writer.matrixA().vector(0).borrowAfter();

		// 第16个
		_pool.borrow();
		writer.matrixA().vector(1).borrowAfter();

		// 第17个
		_pool.borrow();
		writer.matrixA().vector(0).borrowAfter();

		// 第18个
		_pool.borrow();
		writer.matrixA().vector(1).borrowAfter();

		Assertions.assertEquals(16, info.borrowedCount());
		Assertions.assertEquals(16, info.totalCapacity());

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(info, layout);
	}

	private void borrowed18(LayoutWriter writer) {
		borrowed13(writer);

		// 第14个是临时项
		var item0 = _pool.borrow();

		// 本来是6扩容为9的，但是设置了上限8，所以就为8了
		writer.vectorInc(8);

		// 多借出1个后，就扩容了，导致切换到storeA了
		// 注意，由于运行一次，向量池下标就被推进了，所以跟上次比起来，由0变为1
		writer.matrixA().vector(1).borrowTempAfter(item0);

		// 第15个是扩容后的池的第一个
		_pool.borrow();
		writer.matrixA().vector(0).borrowAfter();

		// 第16个
		_pool.borrow();
		writer.matrixA().vector(1).borrowAfter();

		// 第17个
		_pool.borrow();
		writer.matrixA().vector(0).borrowAfter();

		// 第18个
		_pool.borrow();
		writer.matrixA().vector(1).borrowAfter();

	}

	/**
	 * 第3次扩容，矩阵池扩容
	 */
	@Test
	void borrowedThreeInc() {
		var info = resetPool();
		var writer = new LayoutWriter(info);

		borrowed18(writer);

		// 再借一个，就又要扩容了，因为实际借出16个+2个临时项，这时候，向量池的长度达到配置的最大长度
		// 所以，此时是扩容矩阵池了

		// 第19项，是临时项
		var item = _pool.borrow();

		// 由于扩容了一次，所以由A->B
		writer.matrixInc(3);

		// matrix的扩容，会将池指针重置到扩容前的最后一项，这样下次再借的时候，就从扩容里的第1项取向量池了
		writer.matrixB().vector(1).borrowTempAfter(item);

		// 由2扩容到3
		Assertions.assertEquals(3, info.vectorCount());

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(info, layout);
	}

	private void borrowed19(LayoutWriter writer) {
		borrowed18(writer);

		// 第19项，是临时项
		var item = _pool.borrow();

		// 由于扩容了一次，所以由A->B
		writer.matrixInc(3);

		// matrix的扩容，会将池指针重置到扩容前的最后一项，这样下次再借的时候，就从扩容里的第1项取向量池了
		writer.matrixB().vector(1).borrowTempAfter(item);
	}

	@Test
	void threeIncAfterBorrow1() {
		var info = resetPool();
		var writer = new LayoutWriter(info);

		borrowed19(writer);

		// 在总第3次扩容，第1次矩阵扩容后，继续借1个

		// 第20项
		_pool.borrow();

		// 从下标为2，也就是第3个向量池里取出一条数据
		writer.matrixB().vector(2).borrowAfter();

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(info, layout);
	}

	@Test
	void threeIncAfterBorrow2() {
		var info = resetPool();
		var writer = new LayoutWriter(info);

		borrowed19(writer);

		// 在总第3次扩容，第1次矩阵扩容后，继续借1个

		// 第20项
		_pool.borrow();

		// 从下标为2，也就是第3个向量池里取出一条数据
		writer.matrixB().vector(2).borrowAfter();

		// 第21项
		_pool.borrow();

		// 注意，第21项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标0
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		Assertions.assertEquals(0, info.vectorPointer());

		// 第22项
		_pool.borrow();

		// 注意，第22项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标1
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		Assertions.assertEquals(1, info.vectorPointer());

		// 第23项
		_pool.borrow();

		// 注意，第23项数据，那就是正常指向下标2了
		writer.matrixB().vector(2).borrowAfter();

		Assertions.assertEquals(2, info.vectorPointer());

		// 至此，新增的向量里已经取出了4项，还有4项目可用
		Assertions.assertEquals(4, info.matrixLayout().vectors()[2].waitBorrowCount());

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(info, layout);
	}

	/**
	 * 
	 * 第23项，是刚好把新赠的向量池借了一半，切指针指向新增向量池下标（3）的时候
	 * 
	 * @param writer
	 */
	private void borrowed23(LayoutWriter writer) {
		borrowed19(writer);

		// 在总第3次扩容，第1次矩阵扩容后，继续借1个

		// 第20项
		_pool.borrow();

		// 从下标为2，也就是第3个向量池里取出一条数据
		writer.matrixB().vector(2).borrowAfter();

		// 第21项
		_pool.borrow();

		// 注意，第21项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标0
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		// 第22项
		_pool.borrow();

		// 注意，第22项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标1
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		// 第23项
		_pool.borrow();

		// 注意，第23项数据，那就是正常指向下标2了
		writer.matrixB().vector(2).borrowAfter();
	}

	/**
	 * 第三次扩容已满
	 */
	@Test
	void threeIncBorrowedFull() {
		var info = resetPool();
		var writer = new LayoutWriter(info);

		borrowed23(writer);

		// 继续借1个

		// 第24项
		_pool.borrow();

		// 注意，第24项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标0
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		Assertions.assertEquals(0, info.vectorPointer());

		// 第25项
		_pool.borrow();

		// 注意，第25项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标1
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		Assertions.assertEquals(1, info.vectorPointer());

		// 第26项
		_pool.borrow();

		// 注意，第26项数据，那就是正常指向下标2了
		writer.matrixB().vector(2).borrowAfter();

		Assertions.assertEquals(2, info.vectorPointer());

		// 至此，新增的向量里已经取出了7项，还有1项目可用
		Assertions.assertEquals(1, info.matrixLayout().vectors()[2].waitBorrowCount());

		// 第27项
		_pool.borrow();

		// 注意，第27项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标0
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		Assertions.assertEquals(0, info.vectorPointer());

		// 至此，新增的向量里已经取出了8项，还有0项目可用
		Assertions.assertEquals(0, info.matrixLayout().vectors()[2].waitBorrowCount());

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(info, layout);
	}

	private void borrowed27(LayoutWriter writer) {

		borrowed23(writer);

		// 继续借1个

		// 第24项
		_pool.borrow();

		// 注意，第24项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标0
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		// 第25项
		_pool.borrow();

		// 注意，第25项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标1
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

		// 第26项
		_pool.borrow();

		// 注意，第26项数据，那就是正常指向下标2了
		writer.matrixB().vector(2).borrowAfter();

		// 第27项
		_pool.borrow();

		// 注意，第27项数据，实际上也是从第3个向量池里取出来的，只不过是由线程本地指针得到的指向
		// 正常推进一次指针到下标0
		writer.nextVectorPointer();
		writer.matrixB().vector(2).localBorrowAfter();

	}

	/**
	 * 第4次扩容
	 */
	@Test
	void fourthInc() {
		var info = resetPool();
		var writer = new LayoutWriter(info);

		borrowed27(writer);

		var layout = _pool.getLayout();
		ExpectedLayout.assertLayout(info, layout);
	}

}
