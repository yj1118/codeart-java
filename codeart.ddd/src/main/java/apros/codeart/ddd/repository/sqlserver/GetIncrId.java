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
        StringUtil.appendLine(sql, "begin transaction;");
        StringUtil.appendFormat(sql, "if(object_id('[%s]') is null)", increment);
        StringUtil.appendLine(sql, "begin");
        StringUtil.appendLine(sql, "	create table [" + increment + "]([value] [bigint] NOT NULL,CONSTRAINT [PK_"
                + increment
                + "] PRIMARY KEY CLUSTERED ([value] ASC)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]) ON [PRIMARY];");

        StringUtil.appendLine(sql, "end");
        StringUtil.appendFormat(sql, "if(not exists(select 1 from [%s] with(xlock)))", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, "begin");

        StringUtil.appendFormat(sql, " insert into [%s]([value]) values(1);", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, " select cast(1 as bigint) as value;");
        StringUtil.appendLine(sql, "end");

        StringUtil.appendLine(sql, "else");
        StringUtil.appendLine(sql, "begin");

        StringUtil.appendFormat(sql, " update [%s] set [value]=[value]+1;", increment);
        StringUtil.appendLine(sql);

        StringUtil.appendFormat(sql, "select value from [%s] with(nolock);", increment);
        StringUtil.appendLine(sql);

        StringUtil.appendLine(sql, "end");
        StringUtil.appendLine(sql, "commit;");
        return sql.toString();
    }


    public static final GetIncrId Instance = new GetIncrId();

}
