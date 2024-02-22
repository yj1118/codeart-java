package apros.codeart.dto;

import apros.codeart.context.ContextSession;
import apros.codeart.pooling.IReusable;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.StringSegment;
import apros.codeart.util.StringUtil;

class Finder implements IReusable {

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

	public boolean IsEmpty() {
		return _keyPosition < 0;
	}

	@Override
	public void clear() throws Exception {
		_keyPosition = -1;
		_pass = null;
		_finded = false;
	}

	private static final Finder Empty = new Finder(-1, StringSegment.Empty, false);

	private static Pool<Finder> pool = new Pool<Finder>(() -> {
		return new Finder();
	}, PoolConfig.onlyMaxRemainTime(300));

	private static Finder obtain(int keyPosition, StringSegment pass, boolean finded) {
		var item = ContextSession.obtainItem(pool, () -> new Finder());
		item._keyPosition = keyPosition;
		item._pass = pass;
		item._finded = finded;
		return item;
	}

	public static Finder Find(StringSegment code, int startIndex, char key) {
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
					var pass = StringSegment.obtain(code.getSource(), code.getOffset() + startIndex, length);
					return Finder.obtain(pointer - 1, pass, isEndWord);
				}

				length++;
			}
		}

		{
			var pass = StringSegment.obtain(code.getSource(), code.getOffset() + startIndex, length);
			return Finder.obtain(pointer - 1, pass, false);
		}
	}

}
