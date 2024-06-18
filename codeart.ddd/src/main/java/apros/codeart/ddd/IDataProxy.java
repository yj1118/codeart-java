package apros.codeart.ddd;

import java.util.function.Supplier;

public interface IDataProxy extends INotNullObject {

    Object load(String propertyName, Supplier<Object> defaultValue);

    /**
     * 加载属性更改前的值
     *
     * @param propertyName
     * @return
     */
    Object loadOld(String propertyName);

    void save(String propertyName, Object newValue, Object oldValue);

    /// <summary>
    /// 属性的数据是否已被加载
    /// </summary>
    /// <param name="property"></param>
    /// <returns></returns>
    boolean isLoaded(String propertyName);

    DomainObject getOwner();

    void setOwner(DomainObject owner);

    /// <summary>
    /// 拷贝数据代理中的数据
    /// </summary>
    /// <param name="target"></param>
    void copy(IDataProxy target);

    void clear();

    /**
     * 对象是否为快照
     *
     * @return
     */
    boolean isSnapshot();

    /**
     * 对象是否为镜像
     *
     * @return
     */
    boolean isMirror();

    /// <summary>
    /// 数据版本号
    /// </summary>
    int getVersion();

    void setVersion(int value);

    /**
     * 同步版本号
     */
    void syncVersion();
}
