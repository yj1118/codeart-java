package apros.codeart.dto;

import apros.codeart.context.ContextSession;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.StringUtil;

class DTEValue extends DTEntity {

	@Override
	public DTEntityType getType() {
		return DTEntityType.VALUE;
	}

	private Object _value;

	public Object getValue() {
		return _value;
	}

	public void setValue(Object value) {
		this._value = value;
	}

	private DTEValue() {
		this(StringUtil.empty(), null);
	}

	private DTEValue(String name, Object value) {
		super(name);
		this._value = value;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// todo test，原版本中是克隆了value，但是新版本中考虑到value要么是字符串，要么是其他值类型，没有克隆的必要。
		try {
			return obtain(this.getName(), this.getValue());
		} catch (Exception e) {
			throw new CloneNotSupportedException(e.getMessage());
		}
	}

	@Override
	public void clear() throws Exception {
		super.clear();
		_value = null;
	}

	@Override
	public boolean hasData() {
		return _value != null;
	}

	@Override
	public Iterable<DTEntity> finds(QueryExpression query) throws Exception {
		if (query.isSelf() || this.getName().equalsIgnoreCase(query.getSegment()))
			return this.getSelfAsEntities();// 查询自身
		return DTEntity.obtainList();
	}

	@Override
	public String getCode(boolean sequential, boolean outputName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSchemaCode(boolean sequential, boolean outputName) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Pool<DTEValue> pool = new Pool<DTEValue>(() -> {
		return new DTEValue();
	}, PoolConfig.onlyMaxRemainTime(300));

	public static DTEValue obtain(String name, Object value) throws Exception {
		var item = ContextSession.obtainItem(pool, () -> new DTEValue());
		item.setName(name);
		item.setValue(value);
		return item;
	}

}
