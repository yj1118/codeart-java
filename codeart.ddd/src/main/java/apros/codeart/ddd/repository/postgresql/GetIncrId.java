package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.DataAccess;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GetIncrIdQB;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

import java.util.function.Function;

@SafeAccess
class GetIncrId extends GetIncrIdQB {

    private GetIncrId() {
    }

    @Override
    protected String buildImpl(DataTable table) {
        _init.apply(table.name());
       return String.format("SELECT * FROM get_%s_increment();",table.name());
    }

    private static String getFUNCTIONCode(String tableName){
        String increment = String.format("%sIncrement", tableName);

        StringBuilder sql = new StringBuilder();

        StringUtil.appendFormat(sql,"CREATE OR REPLACE FUNCTION get_%s_increment()",tableName);
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql,"RETURNS TABLE(value bigint)");
        StringUtil.appendLine(sql,"LANGUAGE plpgsql");
        StringUtil.appendLine(sql,"AS $$");

        StringUtil.appendLine(sql, "BEGIN");
        StringUtil.appendLine(sql);
        StringUtil.appendFormat(sql, "    CREATE TABLE IF NOT EXISTS \"%s\"", increment);
        StringUtil.appendLine(sql, "(");
        StringUtil.appendLine(sql, "        \"value\" bigint NOT NULL,CONSTRAINT PK_"
                + increment
                + " PRIMARY KEY (\"value\")");
        StringUtil.appendLine(sql, "    );");
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, "    BEGIN");
        StringUtil.appendFormat(sql, "        IF NOT EXISTS (SELECT 1 FROM \"%s\" FOR UPDATE) THEN", increment);
        StringUtil.appendLine(sql);

        StringUtil.appendFormat(sql, "            INSERT INTO \"%s\"(\"value\") VALUES(1);", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, "        ELSE");

        StringUtil.appendMessageFormat(sql, "            UPDATE \"{0}\" SET \"value\"=\"{0}\".\"value\"+1;", increment);
        StringUtil.appendLine(sql);

        StringUtil.appendLine(sql, "        END IF;");
        StringUtil.appendLine(sql, "    END;");
        StringUtil.appendMessageFormat(sql, "    RETURN QUERY SELECT \"{0}\".\"value\" FROM  \"{0}\";", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendFormat(sql, "END $$;");
        return sql.toString();
    }

    private static final Function<String,Boolean> _init = LazyIndexer.init((tableName)->{
        var code = getFUNCTIONCode(tableName);
        DataAccess.current().execute(code);
        return true;
    });

    public static final GetIncrId Instance = new GetIncrId();

}
