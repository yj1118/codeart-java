package com.apros.codeart.dto;

import static com.apros.codeart.runtime.Util.as;

import java.util.Collections;
import java.util.function.Function;

import com.apros.codeart.context.ContextSession;
import com.apros.codeart.util.StringUtil;

final class DTEValue extends DTEntity {

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
		this(null, null);
	}

	private DTEValue(String name, Object value) {
		this.setName(name);
		this._value = value;
	}

	@Override
	public DTEntity cloneImpl() throws Exception {
		// 原版本中是克隆了value，但是新版本中考虑到value要么是字符串，要么是其他值类型，要么是DTObject（仅在此情况下克隆），没有克隆的必要。
		var value = this.getValue();
		var dto = as(value, DTObject.class);
		if (dto != null)
			return obtain(this.getName(), dto.clone());
		return obtain(this.getName(), value);
	}

	@Override
	public void close() throws Exception {
		super.close();
		this.clearData();
	}

	@Override
	public void clearData() {
		_value = null;
	}

	@Override
	public boolean hasData() {
		return _value != null;
	}

	@Override
	public Iterable<DTEntity> finds(QueryExpression query) throws Exception {
		if (query.onlySelf() || this.getName().equalsIgnoreCase(query.getSegment()))
			return this.getSelfAsEntities();// 查询自身
		return Collections.emptyList();
	}

	@Override
	public String getCode(boolean sequential, boolean outputName) throws Exception {
		String name = this.getName();
		StringBuilder code = new StringBuilder();
		if (outputName && !StringUtil.isNullOrEmpty(name))
			code.append(String.format("\"{0}\"", name));
		if (code.length() > 0)
			code.append(":");
		code.append(getValueCode(sequential));

		return code.toString();
	}

	private String getValueCode(boolean sequential) throws Exception {
		var value = this.getValue();
		var dto = as(value, DTObject.class);
		if (dto != null)
			return dto.getCode(sequential, false);
		return JSON.getCode(value);
	}

	@Override
	public String getSchemaCode(boolean sequential, boolean outputName) throws Exception {
		return this.getName();
	}

	public static DTEValue obtain(String name, Object value) {
		return ContextSession.registerItem(new DTEValue(name, value));
	}

	@Override
	public void setMember(QueryExpression query, Function<String, DTEntity> createEntity) throws Exception {
		throw new UnsupportedOperationException("DTEValue.setMember");

	}

	@Override
	public void removeMember(DTEntity e) {
		throw new UnsupportedOperationException("DTEValue.removeMember");

	}

}
