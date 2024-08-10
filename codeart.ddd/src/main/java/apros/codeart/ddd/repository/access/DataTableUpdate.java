package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.DomainBuffer;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.IValueObject;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.i18n.Language;
import apros.codeart.util.Guid;
import apros.codeart.util.ListUtil;

final class DataTableUpdate {

    private DataTable _self;

    public DataTableUpdate(DataTable self) {
        _self = self;
    }

    public void update(DomainObject obj) {
        if (obj == null || obj.isEmpty())
            return;

        DomainObject root = null;
        if (_self.type() == DataTableType.AggregateRoot)
            root = obj;
        if (root == null || root.isEmpty())
            throw new IllegalStateException(
                    Language.strings("apros.codeart.ddd", "PersistentObjectError", obj.getClass().getName()));

        if (obj.isDirty()) {
            _self.checkDataVersion(root);
            onPreDataUpdate(obj);
            if (updateData(root, null, obj)) {
                onDataUpdated(root, obj);
            }
        } else {
            // 如果对象不是脏的，但是要求修改，那么有可能是该对象的引用链上的对象发生了变化，所以我们移除该对象的缓冲
            DomainBuffer.remove(obj.getClass(), DataTableUtil.getObjectId(obj));
        }
    }

    /// <summary>
    /// 修改数据
    /// </summary>
    /// <param name="obj"></param>
    /// <returns></returns>
    boolean updateData(DomainObject root, DomainObject parent, DomainObject obj) {
        boolean isChanged = false;

        var tips = PropertyMeta.getProperties(_self.objectType());

        var data = new MapData();
        for (var tip : tips) {
            var memberIsChanged = updateAndCollectChangedValue(root, parent, obj, tip, data);
            if (!isChanged)
                isChanged = memberIsChanged;
        }

        // this.Mapper.FillUpdateData(obj, data, this);

        if (!data.isEmpty()) {
            if (_self.type() != DataTableType.AggregateRoot) {
                // 补充根键
                data.put(GeneratedField.RootIdName, DataTableUtil.getObjectId(root));
            }

            // 补充主键
            data.put(EntityObject.IdPropertyName, DataTableUtil.getObjectId(obj));

            var sql = getUpdateSql(data);
            DataAccess.current().nativeExecute(sql, data);

            // 更新代理对象中的数据
            ((DataProxyImpl) obj.dataProxy()).originalData().update(data);
        }

        return isChanged;
    }

    private void onPreDataUpdate(DomainObject obj) {
        _self.mapper().onPreUpdate(obj, _self);
    }

    /**
     * 该方法用于修改数据后，更新基表的信息
     *
     * @param root
     * @param obj
     */
    private void onDataUpdated(DomainObject root, DomainObject obj) {
        obj.markClean(); // 修改之后，就干净了

        var id = DataTableUtil.getObjectId(obj);

        // 更新数据版本号
        var data = new MapData();
        if (_self.type() != DataTableType.AggregateRoot) {
            data.put(GeneratedField.RootIdName, DataTableUtil.getObjectId(root));
        }

        data.put(EntityObject.IdPropertyName, id);

        // 更新版本号
        DataAccess.current().nativeExecute(getUpdateVersionSql(), data);

        // 更新代理对象的版本号
        var dataVersion = _self.type() == DataTableType.AggregateRoot ? _self.getDataVersion(id)
                : _self.getDataVersion(DataTableUtil.getObjectId(root), id);

        obj.dataProxy().setVersion(dataVersion);

        if (_self.type() == DataTableType.AggregateRoot) {
            if (obj.isMirror()) {
                // 镜像被修改了，对应的公共缓冲区中的对象也要被重新加载
                DomainBuffer.remove(obj.getClass(), id);
            }
        }

        _self.mapper().onUpdated(obj, _self);
    }

    void updateMember(DomainObject root, DomainObject parent, DomainObject obj) {
        if (obj == null || obj.isEmpty() || !obj.isDirty())
            return;
        if (updateData(root, parent, obj)) {
            onDataUpdated(root, obj);
        }
    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="root"></param>
    /// <param name="parent"></param>
    /// <param name="current"></param>
    /// <param name="tip"></param>
    /// <param name="data"></param>
    /// <returns>当内部成员发生变化，返回true</returns>
    @SuppressWarnings("unchecked")
    private boolean updateAndCollectChangedValue(DomainObject root, DomainObject parent, DomainObject current,
                                                 PropertyMeta tip, MapData data) {
        switch (tip.category()) {
            case DomainPropertyCategory.Primitive: {
                if (current.isPropertyChanged(tip.name())) {
                    var value = DataTableUtil.getPrimitivePropertyValue(current, tip);
                    data.put(tip.name(), value);
                    return true;
                }
            }
            break;
            case DomainPropertyCategory.PrimitiveList: {
                if (current.isPropertyChanged(tip.name())) {
                    // 删除老数据
                    var child = _self.findChild(_self, tip);
                    child.deleteMiddleByMaster(root, current);

                    var value = current.getValue(tip.name());
                    // 仅存中间表
                    var values = DataTableUtil.getValueListData(value, tip.monotype());
                    child.insertMiddle(root, current, values);
                    return true;
                }
            }
            break;
            // case DomainPropertyType.ValueObject:
            // {
            // if (current.IsPropertyChanged(tip.Property))
            // {
            // //删除原始数据
            // DeleteMemberByOriginalData(root, parent, current, tip);
            // //新增数据
            // InsertAndCollectValueObject(root, parent, current, tip, data);
            // return true;
            // }
            // }
            // break;
            case DomainPropertyCategory.AggregateRoot: {
                if (current.isPropertyChanged(tip.name())) {
                    var field = DataTableUtil.getQuoteField(_self, tip.name());
                    Object obj = current.getValue(tip.name());
                    var id = DataTableUtil.getObjectId(obj);
                    data.put(field.name(), id);
                    return true;
                }
            }
            break;
            case DomainPropertyCategory.ValueObject: // 虽然值对象的成员不会变，但是成员的成员也许会改变
            case DomainPropertyCategory.EntityObject: {
                if (current.isPropertyChanged(tip.name())) {
                    var obj = (DomainObject) current.getValue(tip.name());
                    if (tip.category() == DomainPropertyCategory.ValueObject) {
                        ((IValueObject) obj).setPersistentIdentity(Guid.NewGuid());
                    }

                    var id = DataTableUtil.getObjectId(obj);
                    var field = DataTableUtil.getQuoteField(_self, tip.name());
                    data.put(field.name(), id); // 收集外键

                    // 删除原始数据
                    _self.deleteMemberByOriginalData(root, parent, current, tip);

                    // 保存引用数据
                    if (!obj.isEmpty()) {
                        var child = _self.findChild(_self, tip.name());
                        child.insertMember(root, current, obj);
                    }
                    return true;
                } else if (current.isPropertyDirty(tip.name())) {
                    // 如果引用的内聚成员是脏对象，那么需要修改
                    var obj = (DomainObject) current.getValue(tip.name());
                    if (!obj.isEmpty()) {
                        // 从衍生表中找到对象表
                        var child = _self.findChild(_self, tip.name());
                        child.updateMember(root, parent, obj);
                    }
                    return true;
                }
            }
            break;
            case DomainPropertyCategory.AggregateRootList: {
                if (current.isPropertyChanged(tip.name())) {
                    // 删除老数据
                    var child = _self.findChild(_self, tip);
                    child.middle().deleteMiddleByMaster(root, current);

                    // 追加新数据
                    var objs = (Iterable<?>) current.getValue(tip.name());
                    child.middle().insertMiddle(root, current, objs);
                    return true;
                }
            }
            break;
            case DomainPropertyCategory.ValueObjectList: {
                if (current.isPropertyChanged(tip.name())) {
                    // 在删除数据之前，需要预读对象，确保子对象延迟加载的数据也被增加了，否则会引起数据丢失
                    preRead(current);

                    // 引用关系发生了变化，删除重新追加
                    // 这里要注意，需要删除的是数据库的数据，所以要重新读取
                    // 删除原始数据
                    _self.deleteMembersByOriginalData(root, parent, current, tip);

                    // 加入新数据
                    _self.insertMembers(root, parent, current, tip);
                    return true;
                } else if (current.isPropertyDirty(tip.name())) {
                    // 引用关系没变，只是数据脏了
                    _self.updateMembers(root, parent, current, tip);
                    return true;
                }
            }
            break;
            case DomainPropertyCategory.EntityObjectList: {
                if (current.isPropertyChanged(tip.name())) {
                    var targets = (Iterable<IDomainObject>) current.getValue(tip.name());

                    // 加载原始数据
                    var rawData = ((DataProxyImpl) current.dataProxy()).originalData();
                    var raw = (Iterable<IDomainObject>) _self.readMembers(current, tip, null, rawData, QueryLevel.NONE);
                    // 对比
                    var diff = ListUtil.transform(raw, targets);

                    _self.insertMembers(root, parent, current, diff.adds(), tip);

                    _self.deleteMembers(root, parent, current, diff.removes(), tip);

                    _self.updateMembers(root, parent, current, diff.updates(), tip);

                    // 以上3行代码会打乱成员顺序，所以要更新下排序
                    updateOrderIndexs(root, parent, current, targets, tip);// 更新排序

                    return true;
                } else if (current.isPropertyDirty(tip.name())) {
                    // 引用关系没变，只是数据脏了
                    _self.updateMembers(root, parent, current, tip);
                    return true;
                }
            }
            break;
        }
        return false;
    }

    private static void preRead(DomainObject obj) {
        var objectType = obj.getClass();

        var tips = PropertyMeta.getProperties(objectType);
        for (var tip : tips) {
            preReadProperty(obj, tip);
        }
    }

    private static Object preReadProperty(DomainObject current, PropertyMeta tip) {
        switch (tip.category()) {
            case DomainPropertyCategory.Primitive: {
                return DataTableUtil.getPrimitivePropertyValue(current, tip);
            }
            case DomainPropertyCategory.PrimitiveList: {
                return current.getValue(tip.name());
            }
            case DomainPropertyCategory.ValueObject: {
                var obj = (DomainObject) current.getValue(tip.name());
                preRead(obj);
                return obj;
            }
            case DomainPropertyCategory.AggregateRoot: {
                // 仅获得引用即可，不需要完整的预读
                return current.getValue(tip.name());
            }
            case DomainPropertyCategory.EntityObject: {
                var obj = (DomainObject) current.getValue(tip.name());
                preRead(obj);
                return obj;
            }
            case DomainPropertyCategory.AggregateRootList: {
                // 仅获得引用即可，不需要完整的预读
                return current.getValue(tip.name());
            }
            case DomainPropertyCategory.ValueObjectList:
            case DomainPropertyCategory.EntityObjectList: {
                var objs = (Iterable<?>) current.getValue(tip.name());
                for (var obj : objs) {
                    preRead((DomainObject) obj);
                }
                return objs;
            }
        }
        return null;
    }

    /// <summary>
    /// 修改current对应的集合属性
    /// </summary>
    /// <param name="root"></param>
    /// <param name="parent"></param>
    /// <param name="current"></param>
    /// <param name="tip"></param>
    void updateMembers(DomainObject root, DomainObject parent, DomainObject current, PropertyMeta tip) {
        var objs = (Iterable<?>) current.getValue(tip.name());
        updateMembers(root, parent, current, objs, tip);
    }

    void updateMembers(DomainObject root, DomainObject parent, DomainObject current, Iterable<?> members,
                       PropertyMeta tip) {
        for (var member : members) {
            DomainObject obj = (DomainObject) member;
            if (!obj.isEmpty()) {
                var child = _self.findChild(_self, tip.name());
                // 方法内部会检查是否为脏，为脏的才更新
                child.updateMember(root, current, obj);
            }
        }
    }

    private void updateOrderIndexs(DomainObject root, DomainObject parent, DomainObject current, Iterable<?> members,
                                   PropertyMeta tip) {
        // 先删除，再添加
        var propertyName = tip.name();
        DataTable child = null;
        for (var member : members) {
            DomainObject obj = (DomainObject) member;
            if (obj.isEmpty())
                continue;
            if (child == null)
                child = _self.findChild(_self, propertyName);
            // 删除中间表
            child.middle().deleteMiddle(root, current, obj);
        }

        // 重新添加中间表
        if (child != null)
            child.middle().insertMiddle(root, current, members);
    }

    // private void UpdateMiddle(IDomainObject root, IDomainObject master,
    // IEnumerable slaves, PropertyRepositoryAttribute tip)
    // {
    // this.DeleteMiddle()

    // var rootId = GetObjectId(root);
    // var rootIdName = GeneratedField.RootIdName;
    // var slaveIdName = GeneratedField.SlaveIdName;

    // if (master == null || this.Root.IsEqualsOrDerivedOrInherited(this.Master))
    // {
    // int index = 0;
    // foreach (var slave in slaves)
    // {
    // if (slave.IsNull()) continue;
    // var slaveId = GetObjectId(slave);
    // using (var temp = SqlHelper.BorrowData())
    // {
    // var data = temp.Item;
    // data.Add(rootIdName, rootId);
    // data.Add(slaveIdName, slaveId);
    // data.Add(GeneratedField.OrderIndexName, index);
    // if (this.IsEnabledMultiTenancy)
    // data.Add(GeneratedField.TenantIdName, AppSession.TenantId);
    // SqlHelper.Execute(this.GetUpdateSql(data), data);
    // index++;
    // }
    // }
    // }
    // else
    // {
    // var masterIdName = GeneratedField.MasterIdName;
    // var masterId = GetObjectId(master);
    // int index = 0;
    // foreach (var slave in slaves)
    // {
    // if (slave.IsNull()) continue;
    // var slaveId = GetObjectId(slave);
    // using (var temp = SqlHelper.BorrowData())
    // {
    // var data = temp.Item;
    // data.Add(rootIdName, rootId);
    // data.Add(masterIdName, masterId);
    // data.Add(slaveIdName, slaveId);
    // data.Add(GeneratedField.OrderIndexName, index);
    // if (this.IsEnabledMultiTenancy)
    // data.Add(GeneratedField.TenantIdName, AppSession.TenantId);
    // SqlHelper.Execute(this.GetUpdateSql(data), data);
    // index++;
    // }
    // }

    // }
    // }

    private String getUpdateSql(MapData data) {
        var qb = DataSource.getQueryBuilder(UpdateTableQB.class);
        return qb.build(new QueryDescription(data, _self));
    }

    private String getUpdateVersionSql() {

        var qb = DataSource.getQueryBuilder(UpdateDataVersionQB.class);
        return qb.build(new QueryDescription(_self));
    }

    /**
     * 递减引用次数
     *
     * @param rootId
     * @param id
     */
    public void decrementAssociated(Object rootId, Object id) {
        var data = new MapData();
        data.put(GeneratedField.RootIdName, rootId);
        data.put(EntityObject.IdPropertyName, id);

        var builder = DataSource.getQueryBuilder(DecrementAssociatedQB.class);
        var sql = builder.build(new QueryDescription(_self));
        // 递减引用次数不需要复刻
        DataAccess.current().nativeExecute(sql, data);
    }

}
