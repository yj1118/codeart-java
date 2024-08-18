package apros.codeart;

public enum RunPriority {
    /// <summary>
    /// 低优先级
    /// </summary>
    Low((byte) 0),

    /// <summary>
    /// 用户使用的中等优先级
    /// </summary>
    User((byte) 1),

    High((byte) 2),

    /// <summary>
    /// 框架内部的使用的优先级
    /// </summary>
    Framework_Low((byte) 3),

    /// <summary>
    /// 框架内部的使用的优先级
    /// </summary>
    Framework_Medium((byte) 4),

    /// <summary>
    /// 高优先级
    /// </summary>
    Framework_High((byte) 5);

    private final byte value;

    RunPriority(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
