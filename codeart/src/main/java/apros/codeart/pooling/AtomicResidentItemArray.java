package apros.codeart.pooling;

import java.util.concurrent.atomic.AtomicReferenceArray;

class AtomicResidentItemArray {
	private AtomicReferenceArray<ResidentItem> _items;

	public AtomicResidentItemArray(int count) {
		_items = new AtomicReferenceArray<ResidentItem>(new ResidentItem[count]);
	}

	public int length() {
		return _items.length();
	}

	public void setRelease(int i, ResidentItem newValue) {
		_items.setRelease(i, newValue);
	}

	public ResidentItem getAcquire(int i) {
		return _items.getAcquire(i);
	}

	/**
	 * 
	 * 将 {@src} 的 0-{@length}的内容复制到 {@dest}
	 * 
	 * @param src
	 * @param dest
	 * @param length
	 */
	public static void copy(AtomicResidentItemArray src, AtomicResidentItemArray dest, int length) {
		for (var i = 0; i < length; i++) {
			dest.setRelease(i, src.getAcquire(i));
		}
	}
}
