package apros.codeart.pooling;

import apros.codeart.util.ArgumentAssert;

public class PoolConfig {

    private final int _initialVectorCapacity;

    /**
     * 分段容量的初始值
     *
     * @return
     */
    public int initialVectorCapacity() {
        return _initialVectorCapacity;
    }

    private final int _maxVectorCapacity;

    /**
     * 分段容量的最大值（扩容后的上限值）,不能为0，因为分段容量就是一维数组，不能没有限制扩大
     *
     * @return
     */
    public int maxVectorCapacity() {
        return _maxVectorCapacity;
    }

    private final int _initialMatrixCapacity;

    /**
     * 初始分段数量(为了降低配置的复杂性，该值默认为2，且不需要更改)
     *
     * @return
     */
    public int initialMatrixCapacity() {
        return _initialMatrixCapacity;
    }

    private final int _maxMatrixCapacity;

    /**
     * 分段数量最大值，为0表示不限制
     *
     * @return
     */
    public int maxMatrixCapacity() {
        return _maxMatrixCapacity;
    }

    private final int _detectPeriod;

    /**
     * 探测周期，比如每10秒检查一次池是否需要兼容以节约内存
     *
     * @return
     */
    public int detectPeriod() {
        return _detectPeriod;
    }

    private PoolConfig(int initialVectorCapacity, int maxVectorCapacity, int initialMatrixCapacity,
                       int maxMatrixCapacity, int detectPeriod) {

        ArgumentAssert.lessThanOrEqualZero(initialVectorCapacity, "initialVectorCapacity");
        ArgumentAssert.lessThanOrEqualZero(maxVectorCapacity, "maxVectorCapacity");
        ArgumentAssert.lessThanOrEqualZero(initialMatrixCapacity, "initialMatrixCapacity");

        _initialVectorCapacity = initialVectorCapacity;
        _maxVectorCapacity = maxVectorCapacity;
        _initialMatrixCapacity = initialMatrixCapacity;
        _maxMatrixCapacity = maxMatrixCapacity;
        _detectPeriod = detectPeriod;
    }

    public PoolConfig(int initialVectorCapacity, int maxVectorCapacity, int maxMatrixCapacity, int detectPeriod) {
        this(initialVectorCapacity, maxVectorCapacity, 2, maxMatrixCapacity, detectPeriod);
    }

    /**
     * 分段数量起始为2，不限制分段数量的池
     *
     * @param initialVectorCapacity 每个分段的初始容量
     * @param maxVectorCapacity     分段的最大容量
     */
    public PoolConfig(int initialVectorCapacity, int maxVectorCapacity, int detectPeriod) {
        this(initialVectorCapacity, maxVectorCapacity, 2, 0, detectPeriod);
    }

}
