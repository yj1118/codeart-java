package apros.codeart.ddd.repository;

import apros.codeart.TestSupport;
import apros.codeart.ddd.*;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.ddd.virtual.VirtualRoot;
import apros.codeart.dto.DTObject;
import apros.codeart.dto.DTObjects;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRepository<TRoot extends IAggregateRoot> extends PersistRepository
        implements IRepository<TRoot> {

    private Class<? extends TRoot> _rootType;

    public Class<? extends TRoot> rootType() {
        if (_rootType == null) {
            _rootType = getRootTypeImpl();
        }
        return _rootType;
    }

    public AbstractRepository() {
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends TRoot> getRootTypeImpl() {
        return (Class<? extends TRoot>) TypeUtil.getActualType(this.getClass())[0];
    }

    // #region 增加数据

    protected void registerAdded(IAggregateRoot obj) {
        DataContext.getCurrent().registerAdded(obj, this);
    }

    protected void registerRollbackAdd(IAggregateRoot obj) {
        var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Add);
        DataContext.getCurrent().registerRollback(args);
    }

    @SuppressWarnings("unchecked")
    public final TRoot find(Object id, QueryLevel level) {
        return (TRoot) findRoot(id, level);
    }

    @SuppressWarnings("unchecked")
    public final void addRoot(IAggregateRoot obj) {
        add((TRoot) obj);
    }

    public final void add(TRoot obj) {
        if (obj.isEmpty())
            return;

        DataContext.using(() -> {
            registerRollbackAdd(obj);
//            StatusEvent.execute(StatusEventType.PreAdd, obj);
//            obj.onPreAdd();
            registerAdded(obj);
//            obj.onAdded();
//            StatusEvent.execute(StatusEventType.Added, obj);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void persistAdd(IAggregateRoot obj) {
        if (obj.isEmpty())
            return;
        TRoot root = (TRoot) obj;
        if (this.onPrePersist(obj, RepositoryAction.Add)) {
            persistAddRoot(root);
        }
        this.onPersisted(obj, RepositoryAction.Add);
    }

    protected abstract void persistAddRoot(TRoot obj);

//	region 修改数据

    protected final void registerRollbackUpdate(IAggregateRoot obj) {
        var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Update);
        DataContext.getCurrent().registerRollback(args);
    }

    protected final void registerUpdated(IAggregateRoot obj) {
        DataContext.getCurrent().registerUpdated(obj, this);
    }

    @SuppressWarnings("unchecked")
    public final void updateRoot(IAggregateRoot obj) {
        update((TRoot) obj);
    }

    public final void update(TRoot obj) {
        if (obj.isEmpty())
            return;

        DataContext.using(() -> {
            registerRollbackUpdate(obj);
//            StatusEvent.execute(StatusEventType.PreUpdate, obj);
//            obj.onPreUpdate();
            registerUpdated(obj);
//            obj.onUpdated();
//            StatusEvent.execute(StatusEventType.Updated, obj);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void persistUpdate(IAggregateRoot obj) {
        if (obj.isEmpty())
            return;
        TRoot root = (TRoot) obj;
        if (this.onPrePersist(obj, RepositoryAction.Update)) {
            persistUpdateRoot(root);
        }
        this.onPersisted(obj, RepositoryAction.Update);
    }

    protected abstract void persistUpdateRoot(TRoot obj);

//	region 删除数据

    protected final void registerRollbackDelete(IAggregateRoot obj) {
        var args = new RepositoryRollbackEventArgs(obj, this, RepositoryAction.Delete);
        DataContext.getCurrent().registerRollback(args);
    }

    protected final void registerDeleted(IAggregateRoot obj) {
        DataContext.getCurrent().registerDeleted(obj, this);
    }

    @SuppressWarnings("unchecked")
    public final void deleteRoot(IAggregateRoot obj) {
        delete((TRoot) obj);
    }

    public final void delete(TRoot obj) {
        if (obj.isEmpty())
            return;

        DataContext.using(() -> {
            registerRollbackDelete(obj);
//            StatusEvent.execute(StatusEventType.PreDelete, obj);
//            obj.onPreDelete();
            registerDeleted(obj);
//            obj.onDeleted();
//            StatusEvent.execute(StatusEventType.Deleted, obj);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void persistDelete(IAggregateRoot obj) {
        if (obj.isEmpty())
            return;
        TRoot root = (TRoot) obj;
        if (!root.isEmpty()) {
            if (this.onPrePersist(obj, RepositoryAction.Delete)) {
                persistDeleteRoot(root);
            }
            this.onPersisted(obj, RepositoryAction.Delete);
        }
    }

    protected abstract void persistDeleteRoot(TRoot obj);

    /**
     * 仓储的初始化操作
     */
    public void init() {
        //默认的情况下不需要额外的初始化操作
    }


    /**
     * 清理操作，主要是为了提供给测试使用
     */
    @TestSupport
    public void clearUp() {
        //默认的情况下不需要额外的清理操作
    }


    //#region 动态查询的支持

    /**
     * @param config {
     *               "rowSchemaCode": \"{id,name,children:[{....}]}\", 输出的架构码，是个字符串
     *               "condition": "and",
     *               "rules": [
     *               {
     *               "field": "name",
     *               "operator": "like",
     *               "value": "John%"
     *               },
     *               {
     *               "field": "age",
     *               "operator": "equals",
     *               "value": 30
     *               },
     *               {
     *               "condition": "or",
     *               "rules": [
     *               {
     *               "field": "country",
     *               "operator": "equals",
     *               "value": "USA"
     *               },
     *               {
     *               "field": "country",
     *               "operator": "equals",
     *               "value": "Canada"
     *               }
     *               ]
     *               }
     *               ],
     *               order:{
     *               createTime:'desc'
     *               },
     *               inner:[objectField1,objectField2]
     *               }
     * @return
     */
    @SuppressWarnings("unchecked")
    public DTObject findPage(int pageIndex, int pageSize, DTObject config) {

        var e = Expression.create(config);

        var page = DataPortal.query(this.getRootTypeImpl(),
                e.getStatement(),
                pageIndex, pageSize,
                e::fillArg);

        return page.toDTO(e.getRowSchemaCode());
    }


    private static record CriteriaField(String name, Object value) {
    }

    private static class Expression {

        private final String _statement;

        private final ArrayList<CriteriaField> _criteriaFields;

        private final String _rowSchemaCode;

        public String getStatement() {
            return _statement;
        }

        public String getRowSchemaCode() {
            return _rowSchemaCode;
        }

        public void fillArg(MapData arg) {
            if (_criteriaFields == null) return;
            for (CriteriaField field : _criteriaFields) {
                arg.tryPut(field.name(), field.value);
            }
        }

        public Expression(String rowSchemaCode, String statement, ArrayList<CriteriaField> criteriaFields) {
            _rowSchemaCode = rowSchemaCode;
            _statement = statement;
            _criteriaFields = criteriaFields;
        }

        /**
         * @param config {
         *               "select": ["name", "age", "country"],
         *               "condition": "and",
         *               "rules": [
         *               {
         *               "field": "name",
         *               "operator": "like",
         *               "value": "John%"
         *               },
         *               {
         *               "field": "age",
         *               "operator": "equals",
         *               "value": 30
         *               },
         *               {
         *               "condition": "or",
         *               "rules": [
         *               {
         *               "field": "country",
         *               "operator": "equals",
         *               "value": "USA"
         *               },
         *               {
         *               "field": "country",
         *               "operator": "equals",
         *               "value": "Canada"
         *               }
         *               ]
         *               }
         *               ],
         *               order:{
         *               createTime:'desc'
         *               },
         *               inner:[objectField1,objectField2]
         *               }
         * @return
         */
        public static Expression create(DTObject config) {

            var rowSchemaCode = config.getString("rowSchemaCode");

            ArrayList<CriteriaField> criteriaFields = new ArrayList<>();
            StringBuilder statement = new StringBuilder();
            fillCriteria(statement, config.getObjects("criteria", false), criteriaFields);
            fillOrder(statement, config.getObject("order", null));
            fillInner(statement, config.getStrings("inner", null));

            return new Expression(rowSchemaCode, statement.toString(), criteriaFields);
        }

        private static void fillCriteria(StringBuilder statement, DTObjects criteria, List<CriteriaField> criteriaFields) {
            if (criteria == null) return;
            for (var criterion : criteria) {
                var condition = criterion.getString("condition", "and");
                var rules = criterion.getObjects("rules");
                if (rules.size() == 1) {
                    fillCriteriaRule(statement, rules.get(0), criteriaFields);
                } else {
                    statement.append("(");
                    int lastIndex = rules.size() - 1;
                    for (var i = 0; i < rules.size(); i++) {
                        var rule = rules.get(i);
                        fillCriteriaRule(statement, rule, criteriaFields);
                        if (i < lastIndex)
                            StringUtil.appendFormat(statement, " %s ", condition);
                    }
                    statement.append(")");
                }
            }
        }

        private static void fillCriteriaRule(StringBuilder statement, DTObject rule, List<CriteriaField> criteriaFields) {
            String field = rule.getString("field");
            String operator = rule.getString("operator");
            Object value = rule.getValue("value");
            criteriaFields.add(new CriteriaField(field, value));

            if (operator.equals("like")) {
                StringUtil.appendFormat(statement, "@%s{%s like %@%s%}", field, field, field);
            } else {
                StringUtil.appendFormat(statement, "@%s{%s %s %s}", field, field, operator, field);
            }

        }

        private static void fillOrder(StringBuilder statement, DTObject order) {
            if (order == null || order.isEmpty()) return;
            statement.append("[order by");
            order.each((name, value) -> {
                StringUtil.appendFormat(statement, " %s %s,", name, value);
            });
            StringUtil.removeLast(statement);
            statement.append("]");
        }

        private static void fillInner(StringBuilder statement, Iterable<String> fields) {
            if (fields == null || Iterables.size(fields) == 0) return;
            statement.append("[");
            for (String field : fields) {
                StringUtil.appendFormat(statement, "%s,", field);
            }
            StringUtil.removeLast(statement);
            statement.append("]");
        }

    }


    //endregion

}
