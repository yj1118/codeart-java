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

    /// <summary>
    /// 高优先级
    /// </summary>
    High((byte) 2);

    private final byte value;

    RunPriority(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
