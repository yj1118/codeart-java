package apros.codeart.ddd.virtual.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import apros.codeart.dto.DTEList;
import apros.codeart.dto.DTEObject;
import apros.codeart.dto.DTEValue;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.StringUtil;

public final class TypeDefine {

    private final String _metadataCode;

    public String getMetadataCode() {
        return _metadataCode;
    }

    private final List<TypeEntry> _entries;

    public List<TypeEntry> getEntries() {
        return _entries;
    }

    private final TypeIndex _index;

    TypeIndex getIndex() {
        return _index;
    }

    /**
     * 所属元数据
     */
    private TypeDefine _parent;

    public TypeDefine getParent() {
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
    TypeDefine(String metadataCode) {
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

    TypeDefine(ObjectEntry root, String metadataCode, TypeDefine parent) {
        _root = root;
        _metadataCode = metadataCode;
        _parent = parent;
        _index = parent.getIndex();
        var dto = DTObject.readonly(metadataCode);
        _entries = parse(dto.getRoot());
    }

    TypeDefine(String metadataCode, TypeDefine parent) {
        _metadataCode = metadataCode;
        _parent = parent;
        _index = parent.getIndex();
        _root = new ObjectEntry(this);

        var dto = DTObject.readonly(metadataCode);
        _entries = parse(dto.getRoot());
    }

    private List<TypeEntry> parse(DTEObject root) {
        ArrayList<TypeEntry> entries = new ArrayList<TypeEntry>();

        var members = root.getMembers();
        for (var entity : members) {
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

        return Collections.unmodifiableList(entries);
    }

    private TypeEntry createEntry(DTEValue e) {
        var entryName = e.getName(); // 条目名称
        String value = e.getString();
        if (StringUtil.isNullOrEmpty(value))
            throw new IllegalStateException(Language.strings("apros.codeart", "DTONotSpecifyType", getPathName(entryName)));
        value = StringUtil.trim(value);
        if (StringUtil.isNullOrEmpty(value))
            throw new IllegalStateException(Language.strings("apros.codeart", "DTONotSpecifyType", getPathName(entryName)));

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
            return new ValueEntry(this, entryName, typeName, e.getCode(false, true), descriptions);
        }
    }

    private TypeEntry createEntry(DTEObject e) {
        var name = e.getName(); // 条目名称
        var metadataCode = e.getCode(false, true);
        String typeName = getPathName(name);
        return new ObjectEntry(this, name, typeName, metadataCode);
    }

    private String getPathName(String name) {
        return StringUtil.isNullOrEmpty(this.getRoot().getTypeName()) ? name
                : String.format("%s.%s", this.getRoot().getTypeName(), name);
    }

    private TypeEntry createEntry(DTEList e) {
        var name = e.getName(); // 条目名称
        String typeName = getPathName(name);
        if (e.getItems().size() == 0)
            throw new IllegalStateException(Language.strings("apros.codeart", "DTONotSpecifyType", typeName));
        if (e.getItems().size() > 1)
            throw new IllegalStateException(Language.strings("apros.codeart", "DTOListTypeCountError", typeName));

        var metadataCode = getItemMetadataCode(typeName, e.getItems().get(0));
        return new ListEntry(this, name, typeName, metadataCode);
    }

    private String getItemMetadataCode(String listTypeName, DTObject item) {
        var code = new StringBuilder();
        code.append("{");
        code.append(String.format("%s.item:", listTypeName));
        code.append(item.getCode(false, true));
        code.append("}");
        return code.toString();
    }

    public static TypeDefine getMetadata(String metadataCode) {
        return new TypeDefine(metadataCode);
    }

}
