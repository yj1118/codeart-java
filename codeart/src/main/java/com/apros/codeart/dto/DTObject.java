package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;

import com.apros.codeart.context.ContextSession;
import com.apros.codeart.pooling.IReusable;
import com.apros.codeart.pooling.Pool;
import com.apros.codeart.pooling.PoolConfig;

public class DTObject implements IReusable {

	private boolean _isReadOnly;

	public boolean IsReadOnly() {
		return _isReadOnly;
	}

	private void validateReadOnly() {
		if (_isReadOnly)
			throw new IllegalStateException(strings("DTOReadOnly"));
	}

	public DTObject clone() {
		return null;
//        return new DTObject(_root.Clone() as DTEObject, this.IsReadOnly);
	}

	public <T> T getValue() {
		return null;
	}

	public String getCode() {
		return getCode(false, false);
	}

	public String getCode(boolean sequential, boolean outputName) {
		return null;
	}

	public String getSchemaCode(boolean sequential, boolean outputName) {
		return _root.getSchemaCode(sequential, outputName);
	}

	void fillCode(StringBuilder code) {

	}

	private DTEObject _root;

	DTEObject getRoot() {
		return _root;
	}

	DTEntity getParent() {
		return _root.getParent();
	}

	void setParent(DTEntity e) {
		_root.setParent(e);
	}

	public boolean hasData() {
		return false;
	}

	/**
	 * 无视只读标记，强制清理数据
	 * 
	 * @throws Exception
	 */
	void forceClearData() {
		_root.clearData();
	}

	@Override
	public void clear() throws Exception {
		_isReadOnly = false;
	}

	public void clearData() {
		validateReadOnly();
		_root.clearData();
	}

	private static Pool<DTObject> pool = new Pool<DTObject>(() -> {
		return new DTObject();
	}, PoolConfig.onlyMaxRemainTime(300));

	static DTObject obtain() {
		return ContextSession.obtainItem(pool, () -> new DTObject());
	}

	static DTObject obtain(DTEObject root, boolean isReadOnly) {
		var dto = ContextSession.obtainItem(pool, () -> new DTObject());
		dto._root = root;
		dto._isReadOnly = isReadOnly;
		return dto;
	}
}
