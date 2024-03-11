package com.apros.codeart.dto;

import java.util.ArrayList;

import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;

public final class TypeMetadata {

	private String _metadataCode;

	public String getMetadataCode() {
		return _metadataCode;
	}

	private Iterable<TypeEntry> _entries;

	public Iterable<TypeEntry> getEntries() {
		return _entries;
	}

	private TypeIndex _index;

	TypeIndex getIndex() {
		return _index;
	}

	/**
	 * 所属元数据
	 */
	private TypeMetadata _parent;

	public TypeMetadata getParent() {
		return _parent;
	}

	private ObjectEntry _root;

	public ObjectEntry getRoot() {
		return _root;
	}

	public TypeEntry getEntry(String typeName) {
		return _index.get(typeName);
	}

	/// <summary>
	/// 该构造是一切的起点
	/// </summary>
	/// <param name="metadataCode"></param>
	TypeMetadata(String metadataCode) {
		this._metadataCode = metadataCode;
		this._index = new TypeIndex();
		this._root = new ObjectEntry(this);
		var dto = DTObject.readonly(metadataCode);

		var root = dto.getRoot();
		// 设置了根类型的名称
		this._root.setName(root.getName());
		this._root.setTypeName(root.getName());
		this._index.add(this._root); // 对根类型建立索引

		this._entries = parse(root);
	}

	TypeMetadata(ObjectEntry root, String metadataCode, TypeMetadata parent) {
		_root = root;
		_metadataCode = metadataCode;
		_parent = parent;
		_index = parent.getIndex();
		var dto = DTObject.readonly(metadataCode);
		_entries = parse(dto.getRoot());
	}

	TypeMetadata(String metadataCode, TypeMetadata parent) {
		_metadataCode = metadataCode;
		_parent = parent;
		_index = parent.getIndex();
		_root = new ObjectEntry(this);

		var dto = DTObject.readonly(metadataCode);
		_entries = parse(dto.getRoot());
	}

	private ArrayList<TypeEntry> parse(DTEObject root) {
		ArrayList<TypeEntry> entries = new ArrayList<TypeEntry>();

		var entities = root.getMembers();
		for (var entity : entities) {
			var value = TypeUtil.as(entity, DTEValue.class);
			if (value != null) {
				entries.add(createEntry(value));
				continue;
			}

			var obj = TypeUtil.as(entity, DTEObject.class);
			if (obj != null) {
				entries.add(createEntry(obj));
				continue;
			}

			var list = TypeUtil.as(entity, DTEList.class);
			if (list != null) {
				entries.add(createEntry(list));
				continue;
			}
		}

		return entries;
	}

	private TypeEntry createEntry(DTEValue e) {
		var entryName = e.getName(); // 条目名称
		String value = e.getString();
		if (StringUtil.isNullOrEmpty(value))
			throw new IllegalStateException(Language.strings("DTONotSpecifyType", getPathName(entryName)));
		value = StringUtil.trim(value);
		if (StringUtil.isNullOrEmpty(value))
			throw new IllegalStateException(Language.strings("DTONotSpecifyType", getPathName(entryName)));

		TypeEntry target = this.getIndex().get(value);
		if (target != null) {
			// valueCode是已有类型的名称
			var entry = target.clone();
			entry.setName(entryName);
			entry.setTypeName(value);
			entry.setOwner(this);
			return entry;
		} else {
			var temp = StringUtil.trim(value.split(","));

			var typeName = temp.get(0); // 第一项作为类型名
			temp.remove(0);
			var descriptions = temp;
			return new ValueEntry(this, entryName, typeName, e.GetCode(false, true), descriptions);
		}
	}

	private TypeEntry createEntry(DTEObject e) {
		var name = e.Name; // 条目名称
		var metadataCode = e.GetCode(false, true);
		string typeName = GetPathName(name);
		return new ObjectEntry(this, name, typeName, metadataCode);
	}

	private string GetPathName(string name) {
		return string.IsNullOrEmpty(this.Root.TypeName) ? name : string.Format("{0}.{1}", this.Root.TypeName, name);
	}

	private TypeEntry createEntry(DTEList e) {
		var name = e.Name; // 条目名称
		string typeName = GetPathName(name);
		if (e.Items.Count == 0)
			throw new DTOException(string.Format(Strings.DTONotSpecifyType, typeName));
		if (e.Items.Count > 1)
			throw new DTOException(string.Format(Strings.DTOListTypeCountError, typeName));

		var metadataCode = GetItemMetadataCode(typeName, e.Items[0]);
		return new ListEntry(this, name, typeName, metadataCode);
	}

	private string GetItemMetadataCode(string listTypeName, DTObject item)
	  {
	      using (var temp = StringPool.Borrow())
	      {
	          var code = temp.Item;
	          code.Append("{");
	          code.AppendFormat("{0}.item:", listTypeName);
	          code.Append(item.GetCode(false, true));
	          code.Append("}");
	          return code.ToString();
	      }
	  }

}
