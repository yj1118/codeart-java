package com.apros.codeart.dto;

import com.apros.codeart.util.StringSegment;
import com.apros.codeart.util.StringUtil;

class Finder {

	private int _keyPosition;

	public int keyPosition() {
		return _keyPosition;
	}

	public StringSegment _pass;

	/**
	 * 查找该关键词路过的文本
	 * 
	 * @return
	 */
	public StringSegment pass() {
		return _pass;
	}

	public boolean _finded;

	/**
	 * 是否找到了目标字符
	 * 
	 * @return
	 */
	public boolean finded() {
		return _finded;
	}

	private Finder() {
	}

	private Finder(int keyPosition, StringSegment pass, boolean finded) {
		_keyPosition = keyPosition;
		_pass = pass.trim();// trim很重要，可以把{xx:name, xx:name ,xxx:name } 这种格式的无效空格都移除掉
		_finded = finded;
	}

	public boolean isEmpty() {
		return _keyPosition < 0;
	}

	private static final Finder Empty = new Finder(-1, StringSegment.Empty, false);

	public static Finder find(StringSegment code, int startIndex, char key) {
		int pointer = startIndex, lastIndex = code.length() - 1;
		if (pointer > lastIndex)
			return Finder.Empty;

		int length = 0;

		int level = 0;
		boolean isInString = false;
		char startChar = StringUtil.charEmpty();
		while (pointer < code.length()) {
			char current = code.getChar(pointer);
			boolean isStart = pointer == 0;
			boolean isEnd = pointer == lastIndex;
			char prevWord = isStart ? StringUtil.charEmpty() : code.getChar(pointer - 1);

			pointer++;

			if ((current == '"' || current == '\'') && (isStart || prevWord != '\\')) {
				if (startChar == StringUtil.charEmpty() || startChar == current) // 需要一一对应
				{
					isInString = !isInString;
					// pass.Append(word);
					length++;
					startChar = isInString ? current : StringUtil.charEmpty();
					continue;
				}
			}

			if (isInString) {
				// pass.Append(word);
				length++;
			} else {
				if (current == '{' || current == '[')
					level++;
				else if (current == '}' || current == ']')
					level--;

				boolean isEndWord = current == key;

				if ((isEndWord || isEnd) && level == 0) {
					if (!isEndWord) {
						// pass.Append(word);
						length++;
					}
					var pass = new StringSegment(code.getSource(), code.getOffset() + startIndex, length);
					return new Finder(pointer - 1, pass, isEndWord);
				}

				length++;
			}
		}

		{
			var pass = new StringSegment(code.getSource(), code.getOffset() + startIndex, length);
			return new Finder(pointer - 1, pass, false);
		}
	}

}
