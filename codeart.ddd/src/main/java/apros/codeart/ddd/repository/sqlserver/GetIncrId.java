package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.repository.access.*;
import apros.codeart.ddd.repository.access.internal.AccessUtil;
import apros.codeart.ddd.validation.TimePrecisions;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

@SafeAccess
class GetIncrId extends GetIncrIdQB {

    private GetIncrId() {
    }

    @Override
    protected String buildImpl(DataTable table) {
        String increment = String.format("%sIncrement", table.name());

        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, " update [%s] set [value]=[value]+1;", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendFormat(sql, "select value from [%s] with(nolock);", increment);
        StringUtil.appendLine(sql);
        return sql.toString();
    }


    public static final GetIncrId Instance = new GetIncrId();

}
