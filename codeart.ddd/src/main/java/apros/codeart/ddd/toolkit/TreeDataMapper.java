// 稍后实现 todo
//package apros.codeart.ddd.toolkit;
//
//import apros.codeart.ddd.repository.access.AbstractDataMapper;
//import apros.codeart.util.LazyIndexer;
//
//import java.util.function.Function;
//
//public class TreeDataMapper extends AbstractDataMapper {
//
//    public TreeDataMapper() {
//        initSql();
//    }
//
//    private void initSql() {
//        _getUpdateLRForInsertSqlBySqlServer = LazyIndexer.init(getUpdateLRForInsertSqlBySqlServer);
////        _getUpdateLRForDeleteSqlBySqlServer = LazyIndexer.Init < string, string > (GetUpdateLRForDeleteSqlBySqlServer);
////        _getUpdateLRForMoveSqlBySqlServer = LazyIndexer.Init < string, string > (GetUpdateLRForMoveSqlBySqlServer);
////        _getFindParentsSqlBySqlServer = LazyIndexer.Init < string, string > (GetFindParentsSqlBySqlServer);
//    }
//
//
//    /**
//     * 获得左右值的sql
//     */
//    private Function<String, String> _getUpdateLRForInsertSqlBySqlServer;
//
//    private String getUpdateLRForInsertSqlBySqlServer(String tableName) {
//        var idType = SQLServer.Util.GetSqlDbTypeString(this.IdProperty.PropertyType);
//
//        //这里获取lft,rgt的值，并且更新子节点的lft,rgt数据
//        StringBuilder sql = new StringBuilder();
//        sql.AppendFormat("declare @rootId {0}; --根编号", idType);
//        sql.AppendLine();
//        sql.AppendFormat("select @rootId=rootId from dbo.[{0}] where id=@parentId;", tableName); //得到根编号
//        sql.AppendLine();
//        sql.AppendFormat("select id from dbo.[{0}] with(xlock,holdlock) where rootId=@rootId; --锁整个树", tableName);
//        sql.AppendLine();
//        sql.AppendLine("declare @prgt int; --父节点的右值");
//        sql.AppendFormat("select @prgt = rgt from dbo.[{0}] where id=@parentId;", tableName);
//        sql.AppendLine();
//        sql.AppendLine("if(@prgt is null)");
//        sql.AppendLine("begin");
//        sql.AppendLine("	set @prgt=0;");
//        sql.AppendLine("end");
//        sql.AppendLine("else");
//        sql.AppendLine("begin");
//        sql.AppendFormat("	update dbo.[{0}] set rgt=rgt+2 where rgt>=@prgt and rootId=@rootId;", tableName);
//        sql.AppendLine();
//        sql.AppendFormat("	update dbo.[{0}] set lft=lft+2 where lft>=@prgt and rootId=@rootId;", tableName);
//        sql.AppendLine();
//        sql.AppendLine("end");
//        sql.AppendFormat("update dbo.[{0}] set lft=@prgt,rgt=@prgt+1,rootId=@rootId,moving=0 where id=@id;", tableName);
//        return sql.ToString();
//    }
//
//}