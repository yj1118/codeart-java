package apros.codeart.ddd.repository.access;

/**
 * 数据库代理
 */
public interface IDatabaseAgent {

    /**
     * 能创建索引的字符串的最大长度
     *
     * @return
     */
    int getStringIndexableMaxLength();

    /**
     * 包装标示限定符
     *
     * @param field
     * @return
     */
    String qualifier(String field);

    boolean hasQualifier(String field);

    /**
     * 解开标示限定符
     *
     * @param field
     * @return
     */
    String unQualifier(String field);

    ISqlFormat getSqlFormat();


    /**
     * 获得查询器的实现
     *
     * @return
     */
    <T extends IQueryBuilder> T getQueryBuilder(Class<T> qbClass);

    /**
     * 注册查询器
     *
     * @param <T>
     * @param qbClass
     * @param builder
     */
    <T extends IQueryBuilder> void registerQueryBuilder(Class<T> qbClass, IQueryBuilder builder);

    /**
     * 获得页面查询编译器
     *
     * @return
     */
    IQueryPageCompiler getPageCompiler();

//    void init();
//
//    /**
//     *
//     */
//    void drop();
}
