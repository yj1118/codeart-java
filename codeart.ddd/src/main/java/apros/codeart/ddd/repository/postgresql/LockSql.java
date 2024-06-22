package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

import java.util.function.Function;
import java.util.regex.Pattern;

final class LockSql {

    private LockSql() {
    }


    public static String get(QueryLevel level) {
        return switch (level.code()) {
            case QueryLevel.ShareCode -> " FOR SHARE";
            case QueryLevel.SingleCode -> " FOR UPDATE";
            case QueryLevel.HoldCode -> " FOR UPDATE";
            default -> StringUtil.empty(); // None和Mirror 都是无锁模式
        };
    }

}
