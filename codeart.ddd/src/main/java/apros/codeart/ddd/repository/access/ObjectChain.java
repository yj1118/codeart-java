package apros.codeart.ddd.repository.access;

import java.util.LinkedList;
import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public class ObjectChain {

    private final LinkedList<IDataField> _fields;

    private final LinkedList<IDataField> _reverseFields;

    private LinkedList<IDataField> reverseFields() {
        return _reverseFields;
    }

    private final String _path;

    public String path() {
        return _path;
    }

    private final Function<DataTable, String> _getPath = LazyIndexer.init((parent) -> {
        if (this.path().isEmpty())
            return StringUtil.empty();
        
        StringBuilder code = new StringBuilder();
        for (var item : this.reverseFields()) {
//            if (item.equals(parent.memberField()))
//                break;
            code.insert(0, String.format("%s_", item.propertyName()));
            if (item.table().master().equals(parent)) {
                break;
            }
        }

        if (!code.isEmpty())
            StringUtil.removeLast(code);

        return code.toString();
    });

    public ObjectChain(IDataField source) {
        _fields = getFields(source);
        _reverseFields = ListUtil.reverse(_fields);
        _path = getPath();
    }

    private static LinkedList<IDataField> getFields(IDataField source) {
        LinkedList<IDataField> fields = new LinkedList<IDataField>();

        if (source == null)
            return fields;

        var pointer = source;
        while (pointer != null) {
            // 以下代码是防止死循环的关键代码
            // 我们会将当前的pointer与this.MemberField做比较，检查是否为同一个引用点
            // 如果是，那么就清理之前的记录的路径path，重新记录
            // 这样就可以避免类似member.parent.parent.parent的死循环了
            if (isRepeatedReferencePoint(pointer, source)) {
                fields.clear();
            }

            fields.push(pointer);
            pointer = pointer.parentMemberField();
        }
        return fields;
    }

    /**
     * 是否为重复引用点
     *
     * @param a
     * @param b
     * @return
     */
    private static boolean isRepeatedReferencePoint(IDataField a, IDataField b) {
        if (a == b)
            return false; // a == b表示两者是同一个field，但不是重复的引用点
        // 如果成员字段对应的属性名、成员字段对应的表名、成员字段所在的表的名称相同，那么我们认为是同一个引用点
        return a.propertyName().equals(b.propertyName()) && a.tableName().equals(b.tableName())
                && a.masterTableName().equals(b.masterTableName());
    }

    /**
     * 获得相对于表 {@parent} 的对象链的路径代码
     *
     * @param parent
     * @return
     */
    public String getPath(DataTable parent) {
        if (_path.isEmpty())
            return StringUtil.empty();
        return _getPath.apply(parent);
    }

    /**
     * 获取自身的全路径
     *
     * @return
     */
    private String getPath() {
        StringBuilder code = new StringBuilder();
        for (var item : _fields) {
            code.append(String.format("%s_", item.propertyName()));
        }
        if (!code.isEmpty())
            StringUtil.removeLast(code);
        return code.toString();
    }

    public static final ObjectChain Empty = new ObjectChain(null);
}
