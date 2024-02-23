package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.Util.as;
import static com.apros.codeart.util.StringUtil.isNullOrEmpty;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringSegment;
import com.apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

class EntityDeserializer {

	private EntityDeserializer() {
	}

	public static DTEObject deserialize(String code, boolean isReadOnly) throws Exception {
		if (isNullOrEmpty(code))
			return DTEObject.obtain(StringUtil.empty());
		return deserialize(new StringSegment(code), isReadOnly);
	}

	private static DTEObject deserialize(StringSegment code, boolean isReadOnly) throws Exception {
		if (code.isEmpty())
			return DTEObject.obtain(StringUtil.empty());
		var node = parseNode(CodeType.Object, code);

		var entities = new ArrayList<DTEntity>(1); // 就一个成员
		fillEntities(entities, node, isReadOnly);
		return as(ListUtil.first(entities), DTEObject.class);
	}

	private static void fillEntities(AbstractList<DTEntity> entities, CodeTreeNode node, boolean isReadOnly)
			throws Exception {
		var name = JSON.readString(node.getName().toString());
		if (node.getType() == CodeType.Object) {
			var members = new LinkedList<DTEntity>();
			// 收集成员
			for (var item : node.getChilds()) {
				fillEntities(members, item, isReadOnly);
			}

			var obj = DTEObject.obtain(name, members);
			entities.add(obj);
		} else if (node.getType() == CodeType.List) {

			var tempChilds = new ArrayList<DTEntity>(node.getChildsLength());

			// 收集成员
			for (var item : node.getChilds()) {
				fillEntities(tempChilds, item, isReadOnly);
			}

			var childs = new LinkedList<DTObject>();
			for (var e : tempChilds) {
				var item = createDTO(e, isReadOnly);
				childs.add(item);
			}

			var e = DTEList.obtain(name, childs);
			entities.add(e);
		} else {
			Object value = getNodeValue(node);
			var dte = DTEValue.obtain(name, value);
			entities.add(dte);
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
		var name = nv.getName();
		var value = nv.getValue();

		CodeTreeNode node;

		if (CodeTreeNode.isObject(value)) {
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

	private static class NameAndValue {

		private StringSegment _name;

		public StringSegment getName() {
			return _name;
		}

		private StringSegment _value;

		public StringSegment getValue() {
			return _value;
		}

		public NameAndValue(StringSegment name, StringSegment value) {
			this._name = name;
			this._value = value;
		}
	}

	static class CodeTreeNode {

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

		public int getChildsLength() {
			return Iterables.size(_childs);
		}

		public CodeTreeNode(StringSegment name, StringSegment value, CodeType type, Iterable<CodeTreeNode> childs) {
			_name = name;
			_value = value;
			_type = type;
			_childs = childs;
		}

		public CodeTreeNode(StringSegment name, StringSegment value, CodeType type) {
			this(name, value, type, Collections.emptyList());
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
