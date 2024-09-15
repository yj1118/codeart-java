package apros.codeart.ddd.virtual.internal;

public abstract class TypeEntry {

    private String _name;

    /**
     * 条目名称
     *
     * @return
     */
    public String getName() {
        return _name;
    }

    void setName(String name) {
        _name = name;
    }

    private String _typeName;

    /**
     * 获取类型名称
     *
     * @return
     */
    public String getTypeName() {
        return _typeName;
    }

    void setTypeName(String typeName) {
        _typeName = typeName;
    }

    /**
     * 类型条目的元数据代码
     */
    private String _metadataCode;

    public String getMetadataCode() {
        return _metadataCode;
    }

    void setMetadataCode(String metadataCode) {
        _metadataCode = metadataCode;
    }

    /// <summary>
    /// 条目类型
    /// </summary>
    public abstract EntryCategory getCategory();

    /**
     * 所属元数据
     */
    private TypeDefine _owner;

    public TypeDefine getOwner() {
        return _owner;
    }

    void setOwner(TypeDefine owner) {
        _owner = owner;
    }

    TypeIndex getIndex() {
        return _owner.getIndex();
    }

    public TypeEntry getParent() {
        return _owner.getRoot();
    }

    public TypeEntry(TypeDefine owner, String name, String typeName, String metadataCode) {
        _owner = owner;
        _name = name;
        _typeName = typeName;
        _metadataCode = metadataCode;
    }

    public abstract TypeEntry clone();
}
