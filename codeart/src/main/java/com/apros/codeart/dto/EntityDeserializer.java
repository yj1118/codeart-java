package com.apros.codeart.dto;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.TypeUtil.as;
import static com.apros.codeart.util.StringUtil.isNullOrEmpty;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;

import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringSegment;
import com.apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

class EntityDeserializer {

	private EntityDeserializer() {
	}

	public static DTEObject deserialize(String code, boolean readonly) {
		if (isNullOrEmpty(code))
			return readonly ? DTEObject.obtainReadonlyEmpty(StringUtil.empty())
					: DTEObject.obtainEditable(StringUtil.empty());
		return deserialize(new StringSegment(code), readonly);
	}

	private static DTEObject deserialize(StringSegment code, boolean readonly) {
		var node = parseNode(CodeType.Object, code);

		var entities = new ArrayList<DTEntity>(1); // 就一个成员
		fillEntities(entities, node, readonly);
		return as(ListUtil.first(entities), DTEObject.class);
	}

	private static void fillEntities(AbstractList<DTEntity> entities, CodeTreeNode node, boolean readonly) {
		var name = JSON.readString(node.getName().toString());
		if (node.getType() == CodeType.Object) {
			var members = Util.<DTEntity>createList(readonly, Iterables.size(entities));
			// 收集成员
			for (var item : node.getChilds()) {
				fillEntities(members, item, readonly);
			}

			var obj = DTEObject.obtain(readonly, name, members);
			entities.add(obj);
		} else if (node.getType() == CodeType.List) {

			var tempChilds = new ArrayList<DTEntity>(node.getChildsLength());

			// 收集成员
			for (var item : node.getChilds()) {
				fillEntities(tempChilds, item, readonly);
			}

			var childs = Util.<DTObject>createList(readonly, Iterables.size(tempChilds));
			for (var e : tempChilds) {
				var item = createDTO(e, readonly);
				childs.add(item);
			}

			var e = DTEList.obtain(readonly, name, childs);
			entities.add(e);
		} else {
			boolean valueIsString = node.getType() == CodeType.StringValue;
			var value = node.getValue();
			// 如果是字符串，那么要移除首尾引号
			var valueCode = valueIsString ? value.substr(1, value.length() - 2).toString() : value.toString();
			var dte = DTEValue.obtainByCode(name, valueCode, valueIsString);
			entities.add(dte);
		}
	}

	private static StringSegment trimSign(StringSegment code) {
		return code.substr(1, code.length() - 2).trim();
	}

	private static CodeTreeNode parseNode(CodeType parentCodeType, StringSegment nodeCode) {
		var nv = preHandle(parentCodeType, nodeCode);
		var name = nv.getName();
		var value = nv.getValue();

		CodeTreeNode node;

		if (CodeTreeNode.isObject(value)) {
			value = trimSign(value);
			var childs = parseNodes(CodeType.Object, value);
			node = new CodeTreeNode(name, value, CodeType.Object, childs);
		} else if (CodeTreeNode.isList(value)) {
			value = trimSign(value);
			var childs = parseNodes(CodeType.List, value);
			node = new CodeTreeNode(name, value, CodeType.List, childs);
		} else {
			var codeType = CodeTreeNode.getValueType(value);
			node = new CodeTreeNode(name, value, codeType);
		}
		return node;
	}

	private static DTObject createDTO(DTEntity root, boolean readonly) {
		var o = as(root, DTEObject.class);
		if (o != null)
			return DTObject.obtain(o, readonly);

		var members = Util.<DTEntity>createList(readonly, 1);
		members.add(root);

		o = DTEObject.obtain(readonly, StringUtil.empty(), members);
		return DTObject.obtain(o, readonly);
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
	private static NameAndValue preHandle(CodeType parentCodeType, StringSegment nodeCode) {
		StringSegment name, value;
		var code = nodeCode.trim();
		if (CodeTreeNode.isObject(code) || CodeTreeNode.isList(code)) {
			name = StringSegment.Null;
			value = code;
		} else {
			var info = Finder.find(code, 0, ':');
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

		return new NameAndValue(name, value);
	}

	private static Iterable<CodeTreeNode> parseNodes(CodeType parentCodeType, StringSegment code) {
		ArrayList<CodeTreeNode> nodes = new ArrayList<CodeTreeNode>();
		int startIndex = 0;
		var info = Finder.find(code, startIndex, ',');
		while (!info.isEmpty()) {
			nodes.add(parseNode(parentCodeType, info.pass()));
			info = Finder.find(code, info.keyPosition() + 1, ',');
		}

		return nodes;
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

		public static boolean isList(StringSegment code) {
			if (code.startsWith("[")) {
				if (!code.endsWith("]"))
					throw new CodeFormatErrorException(
							strings("codeart", "JSONCodeBracketsError", code.toString(), "[", "]"));
				return true;
			}
			return false;
		}

		public static boolean isObject(StringSegment code) {
			if (code.startsWith("{")) {
				if (!code.endsWith("}"))
					throw new CodeFormatErrorException(
							strings("codeart", "JSONCodeBracketsError", code.toString(), "{", "}"));
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
