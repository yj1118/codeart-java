package apros.codeart.ddd.repository.access.internal;

import apros.codeart.ddd.repository.access.DataSource;

public final class SqlStatement {
    private SqlStatement() {
    }

    /**
     * 包装标示限定符
     *
     * @param field
     * @return
     */
    public static String qualifier(String field) {
        return DataSource.getAgent().qualifier(field);
    }

    /**
     * 解开标示限定符
     *
     * @param field
     * @return
     */
    public static String unQualifier(String field) {
        return DataSource.getAgent().unQualifier(field);
    }


}
