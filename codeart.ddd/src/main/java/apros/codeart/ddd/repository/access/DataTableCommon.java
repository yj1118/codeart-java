package apros.codeart.ddd.repository.access;

import java.util.ArrayList;
import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;

final class DataTableCommon {

    private final DataTable _self;

    public DataTableCommon(DataTable self) {
        _self = self;
    }

    /**
     * 得到所有引用了目标表的中间表信息（目标表作为master存在于中间表中）
     *
     * @return
     */
    public Iterable<DataTable> getQuoteMiddlesByMaster() {
        return _getQuoteMiddlesByMaster.apply(_self);
    }

    private static final Function<DataTable, Iterable<DataTable>> _getQuoteMiddlesByMaster = LazyIndexer.init((target) -> {
        var root = target.root();
        ArrayList<DataTable> tables = new ArrayList<DataTable>();

        var index = new TempDataTableIndex();
        fillQuoteMiddlesByMaster(target, root, tables, index);
        return tables;
    });

    private static void fillQuoteMiddlesByMaster(DataTable target, DataTable current, ArrayList<DataTable> result,
                                                 TempDataTableIndex index) {
        for (var child : current.buildtimeChilds()) {
            if (!index.tryAdd(child))
                continue; // 尝试添加索引失败，证明已经处理，这通常是由于循环引用导致的死循环，用临时索引可以避免该问题
            if (child.middle() != null && child.middle().master().name().equalsIgnoreCase(target.name())) {

                if (ListUtil.find(result, (t) -> t.name().equals(child.middle().name())) == null)
                    result.add(child.middle());
            }
            fillQuoteMiddlesByMaster(target, child, result, index);
        }
    }

}
