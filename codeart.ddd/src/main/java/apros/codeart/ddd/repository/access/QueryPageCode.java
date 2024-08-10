package apros.codeart.ddd.repository.access;

import java.util.Objects;

public record QueryPageCode(String selectSql, String tableSql, String orderSql) {

    @Override
    public int hashCode() {
        // 这里你可以使用 Objects.hash 或手动计算 hashCode
        return Objects.hash(selectSql, tableSql, orderSql);
    }
}