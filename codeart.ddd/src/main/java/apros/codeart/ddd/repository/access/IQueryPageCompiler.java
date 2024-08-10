package apros.codeart.ddd.repository.access;

public interface IQueryPageCompiler {

    String buildPage(QueryPageCode code, int pageIndex, int pageSize);

    String buildCount(QueryPageCode code);

}
