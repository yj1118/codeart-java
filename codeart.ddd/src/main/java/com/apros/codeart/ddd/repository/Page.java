package com.apros.codeart.ddd.repository;

import java.util.ArrayList;
import java.util.function.Function;

import com.apros.codeart.util.ListUtil;
import com.google.common.collect.Iterables;

public class Page<T> {

	private int _pageIndex;

	public int pageIndex() {
		return _pageIndex;
	}

	private int _pageSize;

	public int pageSize() {
		return _pageSize;
	}

	private int _dataCount;

	/**
	 * 数据总数
	 * 
	 * @return
	 */
	public int dataCount() {
		return _dataCount;
	}

	private Iterable<T> _objects;

	/**
	 * 对象
	 * 
	 * @return
	 */
	public Iterable<T> objects() {
		return _objects;
	}

	public int pageCount() {
		if (_dataCount == 0 || _pageSize == 0)
			return 0;
		int count = (int) (_dataCount / _pageSize);
		if (_dataCount % _pageSize > 0)
			count++;
		return count;
	}

	public <S> Page<S> select(Function<T, S> selector) {
		return new Page<S>(this.pageIndex(), this.pageSize(), ListUtil.map(this.objects(), selector), this.dataCount());
	}

	public Page(int pageIndex, int pageSize, Iterable<T> objects, int dataCount) {
		_pageIndex = pageIndex;
		_pageSize = pageSize;
		_objects = objects;
		_dataCount = dataCount;
	}

	/**
	 * 根据页信息计算出翻页对象
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param objects
	 * @return
	 */
	public static <T> Page<T> calculate(int pageIndex, int pageSize, Iterable<T> objects) {
		int dataCount = Iterables.size(objects);

		var start = (pageIndex - 1) * pageSize;
		if (start >= dataCount) {
			return new Page<T>(pageIndex, pageSize, ListUtil.empty(), dataCount);
		}

		var end = start + pageSize - 1;
		if (end >= dataCount)
			end = dataCount - 1;

		ArrayList<T> items = new ArrayList<T>(end - start + 1);

		for (var i = start; i <= end; i++) {
			items.add(Iterables.get(objects, i));
		}
		return new Page<T>(pageIndex, pageSize, items, dataCount);
	}

}
