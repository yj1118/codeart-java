package apros.codeart.ddd;

import apros.codeart.ddd.repository.RepositoryEventArgs;
import apros.codeart.ddd.repository.RepositoryRollbackEventArgs;
import apros.codeart.util.EventHandler;

/**
 * aggregate聚合的根
 * 在每个Aggregate中，选择一个Entity作为根，并通过根控制对聚合内其他对象的所有访问。只允许外部对象保持对根的引用。对内部成员的临时引用可以被传递出去，但仅在一次操作中有效。
 * 由于根控制访问，因此不能绕过它来修改内部对象。 当提交对aggregate边界内部的任何对象的修改时，整个aggregate中的所有固定规则都必须被满足
 */
public interface IAggregateRoot extends IEntityObject {

    /**
     * 对象在仓储中的唯一键，这不是领域驱动的概念，是框架为了优化系统性能追加的属性
     * <p>
     * 在同样的类型下，该值也可以用于判断唯一性
     *
     * @return
     */
    String uniqueKey();

    /**
     * 内聚根是否为无效的
     *
     * @return
     */
    boolean invalid();

    /**
     * 仓储操作回滚
     */
    EventHandler<RepositoryRollbackEventArgs> rollback();

    void onRollback(Object sender, RepositoryRollbackEventArgs e);

    EventHandler<RepositoryEventArgs> preAdd();

    /**
     * 加入仓储之前
     */
    void onPreAdd();

    EventHandler<RepositoryEventArgs> added();

    /**
     * 加入仓储之后
     */
    void onAdded();

    EventHandler<RepositoryEventArgs> preUpdate();

    /**
     * 修改之前
     */
    void onPreUpdate();

    EventHandler<RepositoryEventArgs> updated();

    /**
     * 修改之后
     */
    void onUpdated();

    EventHandler<RepositoryEventArgs> preDelete();

    /**
     * 删除之前
     */
    void onPreDelete();

    EventHandler<RepositoryEventArgs> deleted();

    /**
     * 删除之后
     */
    void onDeleted();

    /**
     * 提交增加操作之前
     *
     * @return
     */
    EventHandler<RepositoryEventArgs> addPreCommit();

    void onAddPreCommit();

    EventHandler<RepositoryEventArgs> addCommitted();

    void onAddCommitted();

    EventHandler<RepositoryEventArgs> updatePreCommit();

    void onUpdatePreCommit();

    EventHandler<RepositoryEventArgs> updateCommitted();

    void onUpdateCommitted();

    EventHandler<RepositoryEventArgs> deletePreCommit();

    void onDeletePreCommit();

    EventHandler<RepositoryEventArgs> deleteCommitted();

    void onDeleteCommitted();
}
