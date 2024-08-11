package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.QueryLevel;

public interface ILockSql {
    String get(QueryLevel level);
}
