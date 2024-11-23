package apros.codeart.ddd;

/**
 * 状态事件的类型
 */
public enum StatusEventType {
    /// <summary>
    /// 构造领域对象之后
    /// </summary>
    Constructed((byte) 1),

    /// <summary>
    /// 领域对象被改变之前
    /// </summary>
    PreChange((byte) 2),

    /// <summary>
    /// 领域对象被改变后
    /// </summary>
    Changed((byte) 3),

    /**
     * 提交新增操作到仓储之前
     */
    PreAdd((byte) 4),

    /// <summary>
    /// 提交新增操作到仓储之后，
    /// 注意，该事件只是领域对象被提交要保存，而不是真的已保存，有可能由于工作单元
    /// 此时还未真正保存到仓储，要捕获真是保存到仓储之后的事件请用Committed版本
    /// </summary>
    Added((byte) 5),

    /// <summary>
    /// 提交修改操作到仓储之前
    /// </summary>
    PreUpdate((byte) 6),
    /// <summary>
    /// 提交修改操作到仓储之后
    /// 注意，该事件只是领域对象被提交要保存，而不是真的已保存，有可能由于工作单元
    /// 此时还未真正保存到仓储，要捕获真是保存到仓储之后的事件请用Committed版本
    /// </summary>
    Updated((byte) 7),

    /// <summary>
    /// 提交删除操作到仓储之前
    /// </summary>
    PreDelete((byte) 8),
    /// <summary>
    /// 提交删除操作到仓储之后
    /// 注意，该事件只是领域对象被提交要保存，而不是真的已保存，有可能由于工作单元
    /// 此时还未真正保存到仓储，要捕获真是保存到仓储之后的事件请用Committed版本
    /// </summary>
    Deleted((byte) 9),

    /// <summary>
    /// 对象被真实提交的前一刻
    /// </summary>
    AddPreCommit((byte) 10),

    /// <summary>
    /// 对象被真实提交到仓储保存后
    /// </summary>
    AddCommitted((byte) 11),

    /// <summary>
    /// 对象被真实提交到仓储修改的前一刻
    /// </summary>
    UpdatePreCommit((byte) 12),

    /// <summary>
    /// 对象被真实提交到仓储修改后
    /// </summary>
    UpdateCommitted((byte) 13),

    /// <summary>
    /// 对象被真实提交到仓储删除后
    /// </summary>
    DeletePreCommit((byte) 14),

    /// <summary>
    /// 对象被真实提交到仓储删除后
    /// </summary>
    DeleteCommitted((byte) 15),

    /// <summary>
    /// 通用的状态事件
    /// </summary>
    Any((byte) 16);

    private final byte _value;

    private StatusEventType(byte value) {
        _value = value;
    }

    public byte getValue() {
        return _value;
    }

}
