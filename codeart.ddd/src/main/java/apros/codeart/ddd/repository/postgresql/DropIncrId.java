package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.DropIncrIdQB;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class DropIncrId extends DropIncrIdQB {
    private DropIncrId() {
    }

    @Override
    protected String buildImpl(DataTable table) {
        return String.format("DROP TABLE IF EXISTS \"%sIncrement\"", table.name());
    }

    public static final DropIncrId Instance = new DropIncrId();

}
