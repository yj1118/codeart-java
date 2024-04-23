package apros.codeart.ddd.metadata;

import java.util.ArrayList;

import apros.codeart.pooling.util.StringPool;
import apros.codeart.util.StringUtil;

final class SchemeCode {
	private SchemeCode() {
	}

	public static String get(ObjectMeta meta) {

		var path = new Path();

		return StringPool.using((code) -> {
			fillCode(meta, code, path);
		});
	}

	private static void fillCode(ObjectMeta meta, StringBuilder code, Path path) {
		var props = meta.properties();

		code.append("{");
		for (var prop : props) {
			switch (prop.category()) {
			case DomainPropertyCategory.Primitive: {
				fillName(code, prop.name());
				code.append(",");
				continue;
			}
			case DomainPropertyCategory.ValueObject:
			case DomainPropertyCategory.AggregateRoot:
			case DomainPropertyCategory.EntityObject: {
				var objectType = prop.monotype();
				if (path.enter(prop.name(), objectType, false)) {
					fillName(code, prop.name());
					code.append(":");
					var objectMeta = ObjectMetaLoader.get(objectType);
					fillCode(objectMeta, code, path);
					code.append(",");

					path.exit();
				}

				continue;
			}
			case DomainPropertyCategory.PrimitiveList: {
				fillName(code, prop.name());
				code.append(":[],");
				continue;
			}
			default:
				var objectType = prop.monotype();
				if (path.enter(prop.name(), objectType, true)) {
					fillName(code, prop.name());
					code.append(":[");
					var objectMeta = ObjectMetaLoader.get(objectType);
					fillCode(objectMeta, code, path);
					code.append("],");

					path.exit();
				}
			}
		}
		if (code.length() > 1)
			StringUtil.removeLast(code);
		code.append("}");
	}

	private static void fillName(StringBuilder code, String name) {
		code.append("\"");
		code.append(name);
		code.append("\"");
	}

	private static class Path {

		private ArrayList<String> _items = new ArrayList<String>();

		private ArrayList<String> _current = new ArrayList<String>();

		public Path() {

		}

		public boolean enter(String propertyName, Class<?> objectType, boolean isCollection) {
			_current.add(String.format("%s-%s-%s", propertyName, objectType.getSimpleName(), isCollection));
			var path = StringUtil.join(".", _current);
			if (_items.contains(path)) {
				_current.removeLast();
				return false;
			}

			// 记录新路径
			_items.add(path);
			return true;
		}

		public void exit() {
			_current.removeLast();
		}

	}

}
