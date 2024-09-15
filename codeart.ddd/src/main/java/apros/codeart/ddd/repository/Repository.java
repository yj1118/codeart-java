package apros.codeart.ddd.repository;

import java.lang.reflect.Method;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.IRepositoryBase;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.cqrs.slave.Brancher;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.virtual.IVirtualRepository;
import apros.codeart.ddd.virtual.VirtualRoot;
import apros.codeart.ddd.virtual.internal.VirtualRepository;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.MethodUtil;
import apros.codeart.util.StringUtil;

public final class Repository {

    private Repository() {
    }

    /**
     * 注册仓储，请确保<paramref name="repository"/>是线程访问安全的
     *
     * @param <T>
     * @param repositoryInterfaceType
     * @param repository
     */
    public static <T extends IRepositoryBase> void register(Class<?> repositoryInterfaceType, T repository) {
        RepositoryFactory.register(repositoryInterfaceType, repository);
    }

    /**
     * 创建一个仓储对象，同一类型的仓储对象会被缓存
     *
     * @param <T>
     * @param repositoryInterfaceType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends IRepositoryBase> T create(Class<T> repositoryInterfaceType) {
        return (T) RepositoryFactory.create(repositoryInterfaceType);
    }

    public static IRepositoryBase createByObjectType(Class<? extends IAggregateRoot> objectType) {
        return RepositoryFactory.getRepositoryByObject(objectType);
    }

    /**
     * 根据对象名称获得对应的仓储
     *
     * @param rootTypeName
     * @return
     */
    public static IRepositoryBase create(String rootTypeName) {
        var objectType = ObjectMetaLoader.get(rootTypeName).objectType();
        return RepositoryFactory.getRepositoryByObject(objectType);
    }

    /**
     * 获得虚拟类型的仓储
     *
     * @param virtualTypeName
     * @return
     */
    public static IVirtualRepository createVirtual(String virtualTypeName) {
        return (IVirtualRepository) create(virtualTypeName);
    }

    /**
     * 得到对象类型对应的仓储上定义的方法
     *
     * @param objectType
     * @param methodName
     * @return
     */
    public static Method getMethodFromRepository(Class<?> objectType, String methodName) {
        if (StringUtil.isNullOrEmpty(methodName))
            return null;
        var repositoryType = RepositoryFactory.getRepositoryByObject(objectType).getClass();

        var method = MethodUtil.resolveByName(repositoryType, methodName);
        if (method == null)
            throw new DomainDrivenException(Language.strings("apros.codeart.ddd", "NoDefineMethodFromRepository",
                    repositoryType.getName(), methodName));
        return method;
    }

    // #region 基础方法的快速调用

    public static void add(IAggregateRoot obj) {
        var repository = createByObjectType(obj.getClass());
        repository.addRoot(obj);
    }

    public static void update(IAggregateRoot obj) {
        var repository = createByObjectType(obj.getClass());
        repository.updateRoot(obj);
    }

    public static void delete(IAggregateRoot obj) {
        var repository = createByObjectType(obj.getClass());
        repository.deleteRoot(obj);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IAggregateRoot> T find(Class<T> objectType, Object id, QueryLevel level) {
        var repository = createByObjectType(objectType);
        return (T) repository.findRoot(id, level);
    }

    // #endregion

    public static VirtualRoot findRemote(String typeName, Object id) {
        return findRemote(typeName, id, QueryLevel.NONE);
    }

    public static VirtualRoot findRemote(String typeName, Object id, QueryLevel level) {
        var obj = findVirtualRoot(typeName, id, level);

        if (obj.isEmpty()) {
            obj = Brancher.getRemoteObject(typeName, id);
        }
        return obj;
    }

    public static VirtualRoot findVirtualRoot(String typeName, Object id, QueryLevel level) {
        var repository = Repository.create(typeName);
        return (VirtualRoot) repository.findRoot(id, level);
    }

    public static VirtualRoot addVirtualRoot(String typeName, DTObject data) {
        return VirtualRepository.addRoot(typeName, data);
    }

    public static VirtualRoot updataVirtualRoot(String typeName, DTObject data) {
        return VirtualRepository.updateRoot(typeName, data);
    }

    public static void deleteVirtualRoot(String typeName, Object id) {
        VirtualRepository.deleteRoot(typeName, id);
    }

}
