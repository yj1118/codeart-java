package apros.codeart.dto;

import java.util.ArrayList;
import java.util.function.Function;

public abstract class DTEntity implements IDTOSchema {

    private String _name;

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    private DTEntity _parent;

    public DTEntity getParent() {
        return _parent;
    }

    public void setParent(DTEntity parent) {
        _parent = parent;
    }

    public DTEntity() {
    }

    /**
     * dto成员是否含有数据
     *
     * @return
     */
    public abstract boolean hasData();

    public abstract DTEntityType getType();

    private Iterable<DTEntity> _selfAsEntities;

    /**
     * @return
     * @throws Exception
     */
    public Iterable<DTEntity> getSelfAsEntities() {
        if (_selfAsEntities == null) {
            var es = new ArrayList<DTEntity>(1);
            es.add(this);
            _selfAsEntities = es;
        }
        return _selfAsEntities;
    }

    public final Object clone() {
        return cloneImpl();
    }

    protected abstract DTEntity cloneImpl();

    public abstract void setMember(QueryExpression query, Function<String, DTEntity> createEntity);

    /**
     * 删除成员
     *
     * @param e
     */
    public abstract void removeMember(DTEntity e);

    /**
     * 根据查找表达式找出dto成员
     *
     * @param query
     * @return
     */
    public abstract Iterable<DTEntity> finds(QueryExpression query);

    /**
     * @param sequential 是否排序输出代码
     * @param outputName 是否输出key值
     */
    public abstract void fillCode(StringBuilder code, boolean sequential, boolean outputName);

    /**
     * 输出架构码
     *
     * @param sequential
     * @param outputName
     */
    public abstract void fillSchemaCode(StringBuilder code, boolean sequential);

    public String getSchemaCode(boolean sequential) {
        var sb = new StringBuilder();
        fillSchemaCode(sb, sequential);
        return sb.toString();
    }

//	public void close() throws Exception {
//		// 解除引用，防止循环引用导致内存泄漏
//		_name = null;
//		_parent = null;
//		_selfAsEntities = null;
//	}

    public abstract void clearData();

    /**
     * 从 entity 从加载数据到自身
     *
     * @param entity
     */
    public abstract void load(DTEntity entity);

    public abstract void setReadonly(boolean value);

}
