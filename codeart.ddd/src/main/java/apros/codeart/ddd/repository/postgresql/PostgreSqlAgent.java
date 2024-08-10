package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.*;
import apros.codeart.ddd.repository.db.*;
import apros.codeart.util.StringUtil;

public class PostgreSqlAgent extends DatabaseAgent {

    private PostgreSqlAgent() {
        // 注入默认的支持
        this.registerQueryBuilder(CreateTableQB.class, CreateTable.Instance);
        this.registerQueryBuilder(DropTableQB.class, DropTable.Instance);
        this.registerQueryBuilder(InsertTableQB.class, InsertTable.Instance);
        this.registerQueryBuilder(DeleteTableQB.class, DeleteTable.Instance);
        this.registerQueryBuilder(ClearTableQB.class, ClearTable.Instance);
        this.registerQueryBuilder(UpdateTableQB.class, UpdateTable.Instance);
        this.registerQueryBuilder(QueryObjectQB.class, QueryObject.Instance);
        this.registerQueryBuilder(QueryCountQB.class, QueryCount.Instance);

        this.registerQueryBuilder(IncrementAssociatedQB.class, IncrementAssociated.Instance);
        this.registerQueryBuilder(DecrementAssociatedQB.class, DecrementAssociated.Instance);
        this.registerQueryBuilder(GetAssociatedQB.class, GetAssociated.Instance);

        this.registerQueryBuilder(GetPrimitiveValuesQB.class, GetPrimitiveValues.Instance);
        this.registerQueryBuilder(GetSlaveIdsQB.class, GetSlaveIds.Instance);
        this.registerQueryBuilder(QueryPageQB.class, QueryPage.Instance);

        this.registerQueryBuilder(GetIncrIdQB.class, GetIncrId.Instance);
        this.registerQueryBuilder(UpdateDataVersionQB.class, UpdateDataVersion.Instance);
    }

    public static final IDatabaseAgent Instance = new PostgreSqlAgent();

    /**
     * 可被索引得字符串得最大长度
     */
    @Override
    public int getStringIndexableMaxLength() {
        return 1200;
    }

    @Override
    public String qualifier(String field) {
        if (!field.startsWith("\""))
            return String.format("\"%s\"", field);
        return field;
    }

    @Override
    public boolean hasQualifier(String field) {
        return field.startsWith("\"");
    }

    @Override
    public String unQualifier(String field) {
        if (field.startsWith("\""))
            return StringUtil.substr(field, 1, (field.length() - 2));
        return field;
    }

    @Override
    public IQueryPageCompiler getPageCompiler() {
        return SqlQueryPageCompiler.INSTANCE;
    }
}
