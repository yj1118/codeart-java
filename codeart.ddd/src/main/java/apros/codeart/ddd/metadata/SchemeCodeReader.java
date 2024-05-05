package apros.codeart.ddd.metadata;

import apros.codeart.dto.DTObject;

final class SchemeCodeReader {
	private SchemeCodeReader() {
	}

	/**
	 * 
	 * 目前仅读取了 {@meta} 的关于持久化方面所必须的信息，以后可以再包含更多的内容
	 * 
	 * @param <T>
	 * @param meta
	 * @param getPropertyNames
	 * @param getPropertyName
	 * @return
	 */
	public static <T> DTObject read(ObjectMeta meta, Iterable<String> propertyNames) {

		var data = DTObject.editable();
		data.setString("name", meta.name());
		data.setByte("category", meta.category().value());

		for (var propertyName : propertyNames) {
			var tip = meta.findProperty(propertyName);
			var tipData = mapProperty(tip);

			data.push("pros", tipData);
		}

		return data;
	}

	private static DTObject mapProperty(PropertyMeta tip) {
		var data = DTObject.editable();
		data.setString("name", tip.name());
		data.setByte("category", tip.category().value());
		data.setString("monotype", tip.monotype().getSimpleName());
		data.setBoolean("lazy", tip.lazy());

		for (var validator : tip.validators()) {
			var val = DTObject.editable();
			val.setString("name", validator.getClass().getSimpleName());
			val.combineObject("data", validator.getData());
			data.push("vals", val);
		}

		return data;
	}

}
