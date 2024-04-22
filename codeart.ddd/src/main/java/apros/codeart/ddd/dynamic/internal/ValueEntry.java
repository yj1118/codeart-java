package apros.codeart.ddd.dynamic.internal;

public class ValueEntry extends TypeEntry {

	private Iterable<String> _descriptions;

	/// <summary>
	/// 描述项的集合
	/// </summary>
	public Iterable<String> getDescriptions() {
		return _descriptions;
	}

	@Override
	public EntryCategory getCategory() {
		return EntryCategory.Value;
	}

	public boolean isString() {
		return this.getTypeName().equals("string") || this.getTypeName().equals("ascii");
	}

	public ValueEntry(TypeDefine owner, String name, String typeName, String metadataCode,
			Iterable<String> descriptions) {
		super(owner, name, typeName, metadataCode);
		_descriptions = descriptions;
	}

	@Override
	public TypeEntry clone() {
		return new ValueEntry(this.getOwner(), this.getName(), this.getTypeName(), this.getMetadataCode(),
				this.getDescriptions());
	}
}
