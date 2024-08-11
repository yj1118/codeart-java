package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.QueryLevel;

public interface ISqlFormat {
    String format(String sql, DataTable table, String tableAlias, QueryLevel level);
}