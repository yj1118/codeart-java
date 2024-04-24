package apros.codeart.ddd.repository.access;

/**
 * {@code paramName} 匹配的参数名称
 * 
 * {@code placeholder} 占位符，用于生成sql语句时替换用
 */
public record SqlIn(String field, String paramName, String placeholder) {
};
