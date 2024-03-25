package com.apros.codeart.ddd.metadata;

public final class MetadataLoader {
	private MetadataLoader() {
	}

	private static boolean loaded = false;

	/**
	 * 加载所有领域对象的元数据
	 */
	public static void load() {
		if (loaded)
			return;
		loaded = true;

		// 在这里找出所有定义的领域对象
		// todo
		ObjectMetaLoader.load(null);
	}

}
