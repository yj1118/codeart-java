package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.ISqlFormat;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.StringUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import static apros.codeart.runtime.Util.propagate;

public abstract class SqlFormatBase implements ISqlFormat {

    @Override
    public String format(String sql, DataTable table, String tableAlias, QueryLevel level) {
        boolean containsShare = sql.contains(" FOR SHARE");
        String tableName = StringUtil.isNullOrEmpty(tableAlias) ? table.name() : tableAlias;

        try {

            if (containsShare) {
                sql = sql.replace(" FOR SHARE", " FOR UPDATE");
            }

            Statement statement = CCJSqlParserUtil.parse(sql);

            if (statement instanceof Select) {
                Select select = (Select) statement;
                SelectBody selectBody = select.getSelectBody();

                ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
                    @Override
                    public void visit(net.sf.jsqlparser.schema.Column column) {
                        String columnName = column.getColumnName();

                        if (!columnName.startsWith("\"")) {
                            if (columnName.contains("_")) {
                                var temp = columnName.split("_");
                                var target = table;
                                StringBuilder tn = new StringBuilder();
                                String fn = null;
                                for (var i = 0; i < temp.length; i++) {
                                    var name = temp[i];
                                    if (i == temp.length - 1) {
                                        var field = target.getField(name, true);
                                        fn = field.name();
                                    } else {
                                        target = target.findChild(name);
                                        StringUtil.appendFormat(tn, "%s_", target.memberField().name());
                                    }
                                }
                                StringUtil.removeLast(tn);
                                String cn = String.format("%s.%s", SqlStatement.qualifier(tn.toString()), SqlStatement.qualifier(fn));
                                column.setColumnName(cn);
                            } else {
                                var field = table.getField(columnName, true);
                                if (field != null) { //这是最后加了一个如果字段不属于表，那么就有可能是条件，比如： enable=true，这个true就是条件，而不是列名
                                    columnName = field.name();
                                    if (column.getTable() == null) {
//                                        if (field != null) columnName = field.name();
                                        String cn = String.format("%s.%s", SqlStatement.qualifier(tableName), SqlStatement.qualifier(columnName));
                                        column.setColumnName(cn);
                                    } else {
//                                        if (field != null) columnName = field.name();
                                        column.setColumnName(SqlStatement.qualifier(columnName));
                                    }
                                }
                            }
                        }

                        super.visit(column);
                    }
                };

                StringBuilder buffer = new StringBuilder();
                SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer);
                expressionDeParser.setSelectVisitor(deparser);
                expressionDeParser.setBuffer(buffer);

                selectBody.accept(deparser);

                sql = buffer.toString();
            }

            if (containsShare) {
                sql = sql.replace(" FOR UPDATE", " FOR SHARE");
            }

            return sql;

        } catch (JSQLParserException e) {
            throw propagate(e);
        }
    }
}
