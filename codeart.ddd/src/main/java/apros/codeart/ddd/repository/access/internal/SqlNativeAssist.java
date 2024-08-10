package apros.codeart.ddd.repository.access.internal;

import apros.codeart.ddd.MapData;
import apros.codeart.util.LazyIndexer;

import java.util.function.Function;

/**
 * 对原生sql提供的辅助型编码，框架内部不会使用该对象
 */
public class SqlNativeAssist {

    private final SqlCondition _condition;

    private final String _commandText;

    private SqlNativeAssist(String sql) {
        _condition = new SqlCondition(sql);
        _commandText = _condition.code();
    }

    public String parse(MapData param) {
        return _condition.process(_commandText, param);
    }

    public static SqlNativeAssist create(String sql) {
        return _create.apply(sql);
    }

    private static final Function<String, SqlNativeAssist> _create = LazyIndexer.init((sql) -> {
        return new SqlNativeAssist(sql);
    });
}
