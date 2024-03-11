package com.apros.codeart.dto;

public class ValueEntry extends TypeEntry {
	/// <summary>
	/// 描述项的集合
	/// </summary>
	public IList<string> Descriptions
	{
	    get;
	    private set;
	}

	public override EntryCategory Category=>EntryCategory.Value;

	public bool IsString
	{
	    get
	    {
	        return this.TypeName == "string" || this.TypeName == "ascii";
	    }
	}

	public ValueEntry(TypeMetadata owner, string name, string typeName, string metadataCode, IList<string> descriptions)
	    : base(owner, name, typeName, metadataCode)
	{
	    this.Descriptions = descriptions;
	}

	public override TypeEntry

	Clone()
	{
	    return new ValueEntry(this.Owner, this.Name, this.TypeName, this.MetadataCode, this.Descriptions);
	}
}
