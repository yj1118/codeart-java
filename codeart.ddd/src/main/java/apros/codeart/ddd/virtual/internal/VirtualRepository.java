package apros.codeart.ddd.virtual.internal;


import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.ConstructorRepositoryImpl;
import apros.codeart.ddd.repository.Page;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.ddd.virtual.VirtualRoot;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;

import static apros.codeart.runtime.Util.propagate;

public class VirtualRepository {

    private VirtualRepository() {
    }

    public static VirtualRoot add(String typeName, DTObject data) {

        var virtualType = ObjectMetaLoader.get(typeName).objectType();

        var obj = constructObject(virtualType);
        obj.load(data);

        var repository = Repository.create(typeName);
        repository.addRoot(obj);

        return obj;
    }


    private static VirtualRoot constructObject(Class<?> objectType) {

        try {
            var constructorTip = ConstructorRepositoryImpl.getTip(objectType, true);
            var constructor = constructorTip.constructor();
            // 远程对象在本地的映射，仓储构造函数一定是无参的
            return (VirtualRoot) constructor.newInstance(ListUtil.emptyObjects());
        } catch (Throwable ex) {
            throw propagate(ex);
        }
    }

    public static VirtualRoot update(String typeName, DTObject data) {

        var id = data.getValue("id");

        var repository = Repository.create(typeName);

        var obj = (VirtualRoot) repository.findRoot(id, QueryLevel.SINGLE);

        if (obj.isEmpty()) return obj;

        // 加载数据，并标记为已改变
        obj.load(data, true);

        repository.updateRoot(obj);

        return obj;
    }

    public static void delete(String typeName, Object id) {
        var repository = Repository.create(typeName);

        var obj = repository.findRoot(id, QueryLevel.SINGLE);

        if (obj.isEmpty()) return;

        repository.deleteRoot(obj);
    }
}
