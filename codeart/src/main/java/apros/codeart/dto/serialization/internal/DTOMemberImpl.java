package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Field;

import apros.codeart.dto.serialization.DTOMember;

class DTOMemberImpl {

	private String _name;

	public String getName() {
		return _name;
	}

	private boolean _isBlob;

	/**
	 * 是否为文件流类型
	 * 
	 * @return
	 */
	public boolean isBlob() {
		return _isBlob;
	}

	public DTOMemberImpl(String name, boolean isBlob) {
		_name = name;
		_isBlob = isBlob;
	}

	public DTOMemberImpl(String name) {
		this(name, false);
	}

	public static DTOMemberImpl get(Field field) {

		var ann = field.getAnnotation(DTOMember.class);

		return ann != null ? new DTOMemberImpl(ann.name(), ann.blob()) : null;
	}
}
