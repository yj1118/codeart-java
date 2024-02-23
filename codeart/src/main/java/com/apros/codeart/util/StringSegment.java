package com.apros.codeart.util;

import com.apros.codeart.pooling.IReusable;

public final class StringSegment implements IReusable {

	private String _source;

	public String getSource() {
		return _source;
	}

	private int _offset;

	public int getOffset() {
		return _offset;
	}

	private int _length;

	public int length() {
		return _length;
	}

	public StringSegment(String source) {
		this(source, 0, source.length());
	}

	public StringSegment(String source, int offset, int length) {
		_source = source;
		_offset = offset;
		_length = length;
	}

	public StringSegment trim() {
		return trimImpl(2);
	}

	public StringSegment trimStart() {
		return trimImpl(0);
	}

	public StringSegment trimEnd() {
		return trimImpl(1);
	}

	private StringSegment trimImpl(int trimType) {
		if (this.isNull())
			return StringSegment.Empty;

		int length = _offset + _length;
		int end = length - 1;
		int start = _offset;
		if (trimType != 1) {
			// 移除前导空白
			start = _offset;
			while (start < length) {
				if (!Character.isWhitespace(getAbsChar(start))) {
					break;
				}
				start++;
			}
		}
		if (trimType != 0) {
			// 移除后置空白
			end = length - 1;
			while (end >= start) {
				if (!Character.isWhitespace(getAbsChar(end))) {
					break;
				}
				end--;
			}
		}
		return createTrimmedSegment(start, end);
	}

	private StringSegment createTrimmedSegment(int start, int end) {
		int length = (end - start) + 1;
		if (length == _length) {
			return this;
		}
		if (length == 0) {
			return Empty;
		}
		return new StringSegment(_source, start, length);
	}

	public boolean startsWith(String value, boolean ignoreCase) {
		if (this.isNull())
			return false;

		if (value.length() == 0) {
			return true;
		}

		if (value.length() > this._length) {
			return false;
		}

		int pointer = 0;
		var length = _offset + _length;
		for (var i = _offset; i < length; i++) {
			var c = getAbsChar(i);
			if (!equals(c, value.charAt(pointer), ignoreCase))
				return false;
			pointer++;
			if (pointer == value.length())
				return true; // 对比完了
		}
		return false;
	}

	public boolean startsWith(String value) {
		return startsWith(value, false);
	}

	/// <summary>
	///
	/// </summary>
	/// <param name="value"></param>
	/// <param name="ignoreCase">忽略大小写</param>
	/// <returns></returns>
	public boolean startsWith(char value, boolean ignoreCase) {
		if (this.isNull())
			return false;

		if (this._length == 0) {
			return false;
		}

		var firstChar = firstChar();
		return equals(firstChar, value, ignoreCase);
	}

	public boolean startsWith(char value) {
		return startsWith(value, false);
	}

	public boolean endsWith(String value, boolean ignoreCase) {
		if (this.isNull())
			return false;

		if (value.length() == 0) {
			return true;
		}

		if (value.length() > _length) {
			return false;
		}

		int length = _offset + _length;
		int pointer = value.length() - 1;
		for (var i = length - 1; i >= _offset; i--) {
			var c = getAbsChar(i);
			if (pointer < 0)
				break;
			if (!equals(c, value.charAt(pointer), ignoreCase))
				return false;
			pointer--;
		}

		return true;
	}

	public boolean endsWith(String value) {
		return endsWith(value, false);
	}

	/// <summary>
	///
	/// </summary>
	/// <param name="value"></param>
	/// <param name="ignoreCase">忽略大小写</param>
	/// <returns></returns>
	public boolean endsWith(char value, boolean ignoreCase) {
		if (this.isNull())
			return false;

		if (this._length == 0) {
			return false;
		}

		var lastChar = lastChar();
		return equals(lastChar, value, ignoreCase);
	}

	public boolean endsWith(char value) {
		return endsWith(value, false);
	}

	public int indexOf(char value, boolean ignoreCase) {
		if (this.isNull())
			return -1;
		if (this._length == 0)
			return -1;

		int length = _offset + _length;

		for (var i = _offset; i < length; i++) {
			var c = getAbsChar(i);
			if (equals(c, value, ignoreCase))
				return i - _offset; // 返回的是相对偏移量
		}
		return -1;
	}

	public int indexOf(char value) {
		return indexOf(value, false);
	}

	public int indexOf(String value, boolean ignoreCase) {
		if (this.isNull())
			return -1;
		if (this._length == 0)
			return -1;

		for (var i = 0; i < this._length; i++) {
			var length = this._length - i;
			if (length < value.length())
				return -1;
			var position = indexOf(i, value, ignoreCase);
			if (position > -1)
				return position;
		}
		return -1;
	}

	private int indexOf(int startIndex, String value, boolean ignoreCase) {
		var count = this._length - startIndex;
		if (count < value.length())
			return -1;

		var offset = _offset + startIndex;
		var length = offset + count;

		int pointer = 0;
		for (var i = offset; i < length; i++) {
			var c = getAbsChar(i);
			if (!equals(c, value.charAt(pointer), ignoreCase))
				return -1;
			pointer++;
			if (pointer == value.length())
				return i - value.length() + 1 - _offset; // 返回的是相对偏移量
		}
		return -1;
	}

	public int indexOf(String value) {
		return indexOf(value, false);
	}

	public StringSegment substr(int startIndex, int length) {
		if (this.isNull())
			return StringSegment.Empty;

		if (startIndex < 0 || startIndex > this._length) {
			throw new ArgumentOutOfRangeException("startIndex");
		}

		if (length < 0 || (startIndex > (this.length() - length))) {
			throw new ArgumentOutOfRangeException("length");
		}

		if (length == 0) {
			return Empty;
		}

		if ((startIndex == 0) && (length == this.length())) {
			return this;
		}

		var offset = this.getOffset() + startIndex;
		return new StringSegment(_source, offset, length);
	}

	public StringSegment substr(int startIndex) {
		return this.substr(startIndex, this._length - startIndex);
	}

	public boolean isNull() {
		return _source == null;
	}

//	private String _value;

	@Override
	public String toString() {
		return StringUtil.substr(_source, _offset, _length);
//		if (_value == null) {
//			if (this.isNull())
//				return null;
//			_value = _source.substring(_offset, _length);
//		}
//		return _value;
	}

	public char firstChar() {
		return getAbsChar(_offset);
	}

	public char lastChar() {
		return getAbsChar(_offset + _length - 1);
	}

	/**
	 * 内部的getChar方法接受的参数是绝对下标
	 * 
	 * @param index
	 * @return
	 */
	private char getAbsChar(int index) {
		return _source.charAt(index);
	}

	/**
	 * 相对下标
	 * 
	 * @param index
	 * @return
	 */
	public char getChar(int index) {
		return getAbsChar(_offset + index);
	}

	private static boolean equals(char a, char b, boolean ignoreCase) {
		if (ignoreCase)
			return Character.toUpperCase(a) == Character.toUpperCase(b);
		return a == b;
	}

	public static final StringSegment Null = new StringSegment(null, 0, 0);

	public boolean isEmpty() {
		return _offset == 0 && _length == 0;
	}

	public static final StringSegment Empty = new StringSegment("", 0, 0);

	@Override
	public void clear() throws Exception {
		_source = null;
		_offset = 0;
		_length = 0;
//		_value = null;

	}

}
