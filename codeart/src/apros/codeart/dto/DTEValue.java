package apros.codeart.dto;

import static apros.codeart.runtime.Util.as;

import apros.codeart.context.ContextSession;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.pooling.util.StringPool;
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
		// 原版本中是克隆了value，但是新版本中考虑到value要么是字符串，要么是其他值类型，要么是DTObject（仅在此情况下克隆），没有克隆的必要。
		try {
			var value = this.getValue();
			var dto = as(value, DTObject.class);
			if (dto != null)
				return obtain(this.getName(), dto.clone());
			return obtain(this.getName(), value);
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
	public String getCode(boolean sequential, boolean outputName) throws Exception {
		String name = this.getName();
		return StringPool.using((code) -> {
			if (outputName && !StringUtil.isNullOrEmpty(name))
				code.append(String.format("\"{0}\"", name));
			if (code.length() > 0)
				code.append(":");
			code.append(getValueCode(sequential));
		});
	}

	private String getValueCode(boolean sequential) throws Exception {
		var value = this.getValue();
		var dto = as(value, DTObject.class);
		if (dto != null)
			return dto.getCode(sequential, false);
		return JSON.getCode(value);
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
