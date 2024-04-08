package com.apros.codeart.ddd;

import com.apros.codeart.dto.DTObject;
import com.apros.codeart.util.ResourceUtil;

public final class DDDConfig {

	private DDDConfig() {

	}

	private static final DTObject _objectMeta;

	/**
	 * objectMeta.json 里的配置
	 * 
	 * @return
	 */
	public static final DTObject objectMeta() {
		return _objectMeta;
	}

	static {
		_objectMeta = ResourceUtil.loadJSON("config/objectMeta.json");
	}
}
