package com.apros.codeart.dto;

import java.util.List;

import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;

public class ObjectEntry extends TypeEntry {
	/**
	 * 对象的元数据
	 */
	private TypeMetadata _metadata;

	public TypeMetadata getMetadata() {
		if (_metadata == null)
			_metadata = createMetadata();
		return _metadata;
	}

	public List<TypeEntry> getChilds() {
		return this.getMetadata().getEntries();
	}

	/// <summary>
	/// 根据成员名称查找成员
	/// </summary>
	/// <param name="name"></param>
	/// <returns></returns>
	public TypeEntry getMemberByName(String name) {
		return ListUtil.find(this.getMetadata().getEntries(), (e) -> e.getName().equalsIgnoreCase(name));
	}

	@Override
	public EntryCategory getCategory() {
		return EntryCategory.Object;
	}

	ObjectEntry(TypeMetadata owner, String name, String typeName, String metadataCode) {

		super(owner, name, typeName, metadataCode);

		if (this.getIndex().contains(typeName))
			return; // 为了避免死循环，对于已经分析过的类型我们不再分析，但是当xx.Metadata的时候，依然会自动分析
		// 这里执行一遍，是为了索引里可以找到类型
		this.getIndex().add(this);
		_metadata = createMetadata();
	}

	ObjectEntry(TypeMetadata metadata) {
		super(metadata, StringUtil.empty(), StringUtil.empty(), metadata.getMetadataCode());
		_metadata = metadata;
	}

	private TypeMetadata createMetadata() {
		return new TypeMetadata(this, this.getMetadataCode(), this.getOwner());
	}

	@Override
	public TypeEntry clone() {
		var entry = new ObjectEntry(this.getOwner(), this.getName(), this.getTypeName(), this.getMetadataCode());
		entry._metadata = this._metadata;
		return entry;
	}
}
