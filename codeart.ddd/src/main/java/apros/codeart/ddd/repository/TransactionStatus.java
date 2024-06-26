package apros.codeart.ddd.repository;

/**
 * 事务状态
 */
public enum TransactionStatus {

    /**
     * 不开启事务，性能高
     */
    None,

    /**
     * 可以与其他线程共享读取，但是不能与阻塞操作共存
     */
    Share,

    /**
     * 延迟事务，只为关键的任务（CUD）开启事务并且这些任务都在最后集中执行,此模式不能将非持久层的查询操作事务化，只能保证CUD任务是事务性的
     */
    Delay,

    /**
     * 即时事务，可以保证所有任务在事务范围内，性能最差，但是可以保证应用层的所有任务都在事务之内
     */
    Timely
}
