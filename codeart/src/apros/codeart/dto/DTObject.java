package apros.codeart.dto;

import java.util.ArrayList;

import apros.codeart.context.ContextSession;
import apros.codeart.pooling.IReusable;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;

public class DTObject implements IReusable {

	private boolean _isReadOnly;

	public boolean IsReadOnly() {
		return _isReadOnly;
	}

	private void ValidateReadOnly() {
		if (_isReadOnly)
			throw new DTOException(Strings.DTOReadOnly);
	}

	public DTObject clone() {
		return null;
//        return new DTObject(_root.Clone() as DTEObject, this.IsReadOnly);
	}

	public String getCode() {
		return getCode(false, false);
	}

	public String getCode(boolean sequential, boolean outputName) {
		return null;
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
	void forceClearData() throws Exception {
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

	static DTObject obtain() throws Exception {
		return ContextSession.obtainItem(pool, () -> new DTObject());
	}

	private static Pool<ArrayList<DTObject>> poolList = new Pool<ArrayList<DTObject>>(() -> {
		return new ArrayList<DTObject>();
	}, PoolConfig.onlyMaxRemainTime(300));

	static ArrayList<DTObject> obtainList() throws Exception {
		return ContextSession.obtainItem(poolList, () -> new ArrayList<DTObject>());
	}

}
