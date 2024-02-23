package apros.codeart.dto;

import static apros.codeart.i18n.Language.strings;
import static apros.codeart.runtime.Util.as;
import static apros.codeart.util.StringUtil.isNullOrEmpty;

import java.util.ArrayList;

import apros.codeart.context.ContextSession;
import apros.codeart.pooling.IReusable;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringSegment;

class EntityDeserializer {

	private EntityDeserializer() {
	}

	public static DTEObject deserialize(String code, boolean isReadOnly) throws Exception {
		if (isNullOrEmpty(code))
			return DTEObject.obtain();
		return deserialize(StringSegment.obtain(code), isReadOnly);
	}

	private static DTEObject deserialize(StringSegment code, boolean isReadOnly) throws Exception {
		if (code.isEmpty())
			return DTEObject.obtain();
		var node = parseNode(CodeType.Object, code);

		DTEObject result = DTEntity.usingList((entities) -> {
			fillEntities(entities, node, isReadOnly);
			return as(ListUtil.first(entities), DTEObject.class);
		});
		return result;
	}

	private static void fillEntities(ArrayList<DTEntity> entities, CodeTreeNode node, boolean isReadOnly) {
		var name = JSON.readString(node.getName().toString());
		if (node.getType() == CodeType.Object) {
			var members = DTEntity.obtainList();
			// 收集成员
			for (var item : node.getChilds()) {
				fillEntities(members, item, isReadOnly);
			}

			var obj = DTEObject.obtain(members);
			obj.setName(name);
			entities.add(obj);
		} else if (node.getType() == CodeType.List) {
			var childs = DTObject.obtainList();
			DTEntity.usingList((tempChilds) -> {
				// 收集成员
				for (var item : node.getChilds()) {
					fillEntities(tempChilds, item, isReadOnly);
				}

				for (var e : tempChilds) {
					var item = createDTO(e, isReadOnly);
					childs.add(item);
				}
			});

			var e = DTEList.obtain(childs);
			e.setName(name);
			entities.add(e);
		} else {
			Object value = getNodeValue(node);
			var dte = new DTEValue(name, value);
			entities.Add(dte);
		}
	}

	private static Object getNodeValue(CodeTreeNode node) throws Exception {
		if (node.getType() == CodeType.StringValue) {
			var value = JSON.readString(node.getValue().toString());
			var time = JSON.parseInstant(value);
			if (time != null)
				return time; // 有可能是客户端的JS库的JSON.Parse处理后得到的时间，得特别处理
			return value;
		}
		return JSON.getValueByNotString(node.getValue().toString());
	}

	private static CodeTreeNode parseNode(CodeType parentCodeType, StringSegment nodeCode) {
		var nv = preHandle(parentCodeType, nodeCode);
		var name = nv.Name;
		var value = nv.Value;

		CodeTreeNode node;

		if (CodeTreeNode.IsObject(value)) {
			value = TrimSign(value);
			var childs = ParseNodes(CodeType.Object, value);
			node = new CodeTreeNode(name, value, CodeType.Object, childs);
		} else if (CodeTreeNode.IsList(value)) {
			value = TrimSign(value);
			var childs = ParseNodes(CodeType.List, value);
			node = new CodeTreeNode(name, value, CodeType.List, childs);
		} else {
			var codeType = CodeTreeNode.GetValueType(value);
			node = new CodeTreeNode(name, value, codeType);
		}
		return node;
	}

	private static DTObject createDTO(DTEntity root, boolean isReadOnly) {
		var o = as(root, DTEObject.class);
		if (o != null)
			return DTObject.obtain(o, isReadOnly);

		var members = DTEntity.obtainList();
		members.add(root);

		o = DTEObject.obtain(members);
		return DTObject.obtain(o, isReadOnly);
	}

	/**
	 * 预处理节点代码
	 * 
	 * @param parentCodeType
	 * @param nodeCode
	 * @return
	 * @throws Exception
	 * @throws CodeFormatErrorException
	 */
	private static NameAndValue preHandle(CodeType parentCodeType, StringSegment nodeCode)
			throws CodeFormatErrorException, Exception {
		StringSegment name, value;
		var code = nodeCode.trim();
		if (CodeTreeNode.isObject(code) || CodeTreeNode.isList(code)) {
			name = StringSegment.Null;
			value = code;
		} else {
			var info = Finder.Find(code, 0, ':');
			boolean isStringValue = CodeTreeNode.getValueType(nodeCode) == CodeType.StringValue;

			if (parentCodeType == CodeType.Object && !isStringValue) // 如果是{aaa}，我们会将aaa识别为name，如果是{'aaa'}，我们会将aaa识别为value
			{
				name = !info.finded() ? code : code.substr(0, info.keyPosition()).trim();
				value = !info.finded() ? StringSegment.Null : code.substr(info.keyPosition() + 1).trim();
			} else {
				name = !info.finded() ? StringSegment.Null : code.substr(0, info.keyPosition()).trim();
				value = !info.finded() ? code : code.substr(info.keyPosition() + 1).trim();
			}
		}

		return NameAndValue.obtain(name, value);
	}

	private static class NameAndValue extends IReusable {

		private StringSegment _name;

		public StringSegment getName() {
			return _name;
		}

		private StringSegment _value;

		public StringSegment getValue() {
			return _value;
		}

		private NameAndValue() {
		}

//		private NameAndValue(StringSegment name, StringSegment value) {
//			this._name = name;
//			this._value = value;
//		}

		public void clear() throws Exception {
			_name = null;
			_value = null;
		}

		private static Pool<NameAndValue> pool = new Pool<NameAndValue>(() -> {
			return new NameAndValue();
		}, PoolConfig.onlyMaxRemainDefaultTime());

		public static NameAndValue obtain(StringSegment name, StringSegment value) {
			var item = ContextSession.obtainItem(pool, () -> new NameAndValue());
			item._name = name;
			item._value = value;
			return item;
		}

	}

	static class CodeTreeNode extends IReusable {

		private StringSegment _name;

		public StringSegment getName() {
			return _name;
		}

		private StringSegment _value;

		public StringSegment getValue() {
			return _value;
		}

		private CodeType _type;

		public CodeType getType() {
			return _type;
		}

		private Iterable<CodeTreeNode> _childs;

		public Iterable<CodeTreeNode> getChilds() {
			return _childs;
		}

//		public CodeTreeNode(StringSegment name, StringSegment value, CodeType type, CodeTreeNode[] childs) {
//			this.Name = name;
//			this.Value = value;
//			this.Type = type;
//			this.Childs = childs;
//		}

//	public CodeTreeNode(StringSegment name, StringSegment value, CodeType type)
//	        : this(name, value, type, Array.Empty<CodeTreeNode>())
//	    {
//	    }

		public void clear() throws Exception {
			_name = null;
			_value = null;
			_childs = null;
		}

//		#region 静态成员

		public static boolean isList(StringSegment code) throws CodeFormatErrorException {
			if (code.startsWith("[")) {
				if (!code.endsWith("]"))
					throw new CodeFormatErrorException(strings("JSONCodeBracketsError", code.toString(), "[", "]"));
				return true;
			}
			return false;
		}

		public static boolean isObject(StringSegment code) throws CodeFormatErrorException {
			if (code.startsWith("{")) {
				if (!code.endsWith("}"))
					throw new CodeFormatErrorException(strings("JSONCodeBracketsError", code.toString(), "{", "}"));
				return true;
			}
			return false;
		}

		/**
		 * 获取JSON格式的值
		 * 
		 * @param code
		 * @return
		 */
		public static CodeType getValueType(StringSegment code) {
			var codeType = CodeType.NonStringValue;
			if (code.length() >= 2) {
				if ((code.startsWith('\"') && code.endsWith('\"')) || (code.startsWith('\'') && code.endsWith('\''))) {
					codeType = CodeType.StringValue;
				}
			}
			return codeType;
		}

//		#endregion
	}

}
