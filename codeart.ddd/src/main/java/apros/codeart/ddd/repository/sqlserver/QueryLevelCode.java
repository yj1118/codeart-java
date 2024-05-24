package apros.codeart.ddd.repository.sqlserver;

import java.util.function.Function;
import java.util.regex.Pattern;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

final class LockSql {

	private LockSql() {
	}

	public static String get(String sql, QueryLevel level) {
		if (level == null)
			return sql;
		return _getLevelSql.apply(sql).apply(level);
	}

	private final static Pattern _reg = Pattern.compile(".+from[ ](.+?)(where|inner|left)", Pattern.CASE_INSENSITIVE);

	private static Function<String, Function<QueryLevel, String>> _getLevelSql = LazyIndexer.init((sql) -> {
		return LazyIndexer.init((level) -> {

			var math = _reg.matcher(sql);
			if (!math.find())
				throw new IllegalStateException(Language.strings("apros.codeart.ddd", "FailedParseLock"));
			var tableName = math.group(1);
			var index = math.start(1);

			return StringUtil.insert(sql, index + tableName.length(), get(level));
		});
	});

	public static String get(QueryLevel level) {
		switch (level.code()) {
		case QueryLevel.ShareCode:
			return StringUtil.empty();
		case QueryLevel.SingleCode:
			return " with(xlock,rowlock) ";
		case QueryLevel.HoldSingleCode:
			return " with(xlock,holdlock) ";
		default:
			return " with(nolock) "; // None和Mirror 都是无锁模式
		}
	}

}
