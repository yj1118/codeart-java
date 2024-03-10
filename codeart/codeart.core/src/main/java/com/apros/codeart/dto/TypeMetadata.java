package com.apros.codeart.dto;

public final class TypeMetadata {

	private String _metadataCode;

	public String getMetadataCode() {
		return _metadataCode;
	}

	private ArrayList<TypeEntry> _entries;

	public ArrayList<TypeEntry> getEntries() {
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

	public TypeEntry getEntry(String typeName)
	  {
	      TypeEntry entry = null;
	      if (_index.TryGet(typeName, out entry)) return entry;
	      return null;
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

	internal TypeMetadata(ObjectEntry root, string metadataCode, TypeMetadata parent) {
		this.Root = root;
		this.MetadataCode = metadataCode;
		this.Parent = parent;
		this.Index = parent.Index;
		var dto = DTObject.Create(metadataCode);
		this.Entries = Parse(dto.GetRoot());
	}

	internal TypeMetadata(string metadataCode, TypeMetadata parent) {
		this.MetadataCode = metadataCode;
		this.Parent = parent;
		this.Index = parent.Index;
		this.Root = new ObjectEntry(this);

		var dto = DTObject.Create(metadataCode);
		this.Entries = parse(dto.GetRoot());
	}

	private ArrayList<TypeEntry> parse(DTEObject root)
	  {
		ArrayList<TypeEntry> entries = new ArrayList<TypeEntry>();

	      var entities = root.getEntities();
	      for (var entity : entities)
	      {
	          var value = entity as DTEValue;
	          if (value != null)
	          {
	              entries.Add(createEntry(value));
	              continue;
	          }

	          var obj = entity as DTEObject;
	          if (obj != null)
	          {
	              entries.Add(createEntry(obj));
	              continue;
	          }

	          var list = entity as DTEList;
	          if(list != null)
	          {
	              entries.Add(createEntry(list));
	              continue;
	          }

	      }

	      return entries;
	  }

	private TypeEntry createEntry(DTEValue e)
	  {
	      var entryName = e.getName(); //条目名称
	      if (e.Value == null) throw new DTOException(string.Format(Strings.DTONotSpecifyType, GetPathName(entryName)));
	      var valueCode = e.Value.ToString().Trim();
	      if (valueCode.Length == 0) throw new DTOException(string.Format(Strings.DTONotSpecifyType, GetPathName(entryName)));

	      TypeEntry target = null;
	      if (this.Index.TryGet(valueCode, out target))
	      {
	          //valueCode是已有类型的名称
	          var entry = target.Clone();
	          entry.Name = entryName;
	          entry.TypeName = valueCode;
	          entry.Owner = this;
	          return entry;
	      }
	      else
	      {
	          var temp = valueCode.Split(',').Select((t) => t.Trim()).ToList();
	          var typeName = temp[0]; //第一项作为类型名
	          temp.RemoveAt(0);
	          var descriptions = temp;
	          return new ValueEntry(this, entryName, typeName, e.GetCode(false, true), descriptions);
	      }
	  }

	private TypeEntry CreateEntry(DTEObject e) {
		var name = e.Name; // 条目名称
		var metadataCode = e.GetCode(false, true);
		string typeName = GetPathName(name);
		return new ObjectEntry(this, name, typeName, metadataCode);
	}

	private string GetPathName(string name) {
		return string.IsNullOrEmpty(this.Root.TypeName) ? name : string.Format("{0}.{1}", this.Root.TypeName, name);
	}

	private TypeEntry CreateEntry(DTEList e) {
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
