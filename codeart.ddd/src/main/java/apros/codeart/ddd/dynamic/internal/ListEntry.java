package apros.codeart.ddd.dynamic.internal;

import apros.codeart.util.ListUtil;

public class ListEntry extends TypeEntry {

	private TypeDefine _itemMetadata;

	/// <summary>
	/// 对象的元数据
	/// </summary>
	public TypeDefine getItemMetadata() {
		if (_itemMetadata == null)
			init();
		return _itemMetadata;
	}

	private TypeEntry _itemEntry;

	public TypeEntry getItemEntry() {
		if (_itemEntry == null)
			init();
		return _itemEntry;
	}

	@Override
	public EntryCategory getCategory() {
		return EntryCategory.List;
	}

	ListEntry(TypeDefine owner, String name, String typeName, String metadataCode) {
		super(owner, name, typeName, metadataCode);
		if (this.getIndex().contains(typeName))
			return; // 为了避免死循环，对于已经分析过的类型我们不再分析，但是当xx.Metadata的时候，依然会自动分析
		// 这里执行一遍，是为了索引里可以找到类型
		this.getIndex().add(this);
		init();
	}

	private void init() {
		_itemMetadata = new TypeDefine(this.getMetadataCode(), this.getOwner());
		_itemEntry = ListUtil.first(_itemMetadata.getEntries());
	}

	@Override
	public TypeEntry clone() {
		return new ListEntry(this.getOwner(), this.getName(), this.getTypeName(), this.getMetadataCode());
	}
}
