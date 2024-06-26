package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.DbType;
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

public final class DBUtil {
    private DBUtil(){}

    /**
     * 由于用户写的对象表达式里的属性是不带postgresql的标识符的，这会执行报错，所以得加上
     * @param sql
     * @return
     */
    public static String addQualifier(String sql, DataTable table){
        boolean containsShare = sql.contains(" FOR SHARE");

        try {

            if(containsShare){
                sql = sql.replace(" FOR SHARE"," FOR UPDATE");
            }

            Statement statement = CCJSqlParserUtil.parse(sql);

            if (statement instanceof Select) {
                Select select = (Select) statement;
                SelectBody selectBody = select.getSelectBody();

                ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
                    @Override
                    public void visit(net.sf.jsqlparser.schema.Column column) {
                        String columnName = column.getColumnName();

                        if(!columnName.startsWith("\"")){
                            if(columnName.contains("_")){
                                var temp = columnName.split("_");
                                var target = table;
                                StringBuilder tn = new StringBuilder();
                                String fn = null;
                                for(var i=0;i<temp.length;i++){
                                    var name = temp[i];
                                    if(i == temp.length-1){
                                        var field = target.getField(name,true);
                                        fn = field.name();
                                    }
                                    else{
                                        target = target.findChild(name);
                                        StringUtil.appendFormat(tn,"%s_",target.memberField().name());
                                    }
                                }
                                StringUtil.removeLast(tn);
                                String cn = String.format("%s.%s",SqlStatement.qualifier(tn.toString()),SqlStatement.qualifier(fn));
                                column.setColumnName(cn);
                            }
                            else{
                                var field =  table.getField(columnName,true);
                                if(column.getTable() == null){
                                    if(field != null) columnName = field.name();
                                    String cn = String.format("%s.%s",SqlStatement.qualifier(table.name()),SqlStatement.qualifier(columnName));
                                    column.setColumnName(cn);
                                }
                                else {
                                    if(field != null) columnName = field.name();
                                    column.setColumnName(SqlStatement.qualifier(columnName));
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

            if(containsShare){
                sql = sql.replace(" FOR UPDATE"," FOR SHARE");
            }

            return sql;

        } catch (JSQLParserException e) {
            throw propagate(e);
        }
    }

    public static boolean needInc(DataTable table){
        var idField = table.idField();
        if(idField != null){
            var idType = idField.dbType();
            return idType ==  DbType.Int64 || idType ==  DbType.Int32 || idType == DbType.Int16;
        }
        return  false;
    }

}
