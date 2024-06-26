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
       return String.format("SELECT * FROM get_%s_increment();",table.name());
    }

    public static final GetIncrId Instance = new GetIncrId();

}
