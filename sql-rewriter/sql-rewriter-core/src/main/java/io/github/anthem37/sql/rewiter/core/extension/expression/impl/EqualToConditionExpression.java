package io.github.anthem37.sql.rewiter.core.extension.expression.impl;

import io.github.anthem37.sql.rewiter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewiter.core.util.JsqlParserUtils;
import lombok.Getter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * 等值条件表达式（=）
 * <p>
 * 用于表示SQL中的等值比较，例如 {@code table_name.column_name = value}。
 * </p>
 *
 * @author hb28301
 * @since 2025/11/12 15:17:53
 */
public class EqualToConditionExpression extends EqualsTo implements IConditionExpression {

    /**
     * 表名（区分大小写，建议与SQL中表名保持一致）
     * 用于生成左侧的Column对象，支持别名适配。
     */
    @Getter
    private final String tableName;
    /**
     * 字段名
     */
    @Getter
    private final String columnName;
    /**
     * 值（支持任意类型，最终会转为SQL表达式）
     */
    @Getter
    private final Object columnValue;

    /**
     * 构造等值条件表达式
     *
     * @param tableName   表名
     * @param columnName  字段名
     * @param columnValue 值
     */
    public EqualToConditionExpression(String tableName, String columnName, Object columnValue) {
        super();
        setLeftExpression(new Column(new Table(tableName), columnName));
        setRightExpression(JsqlParserUtils.createValueExpression(columnValue));
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnValue = columnValue;
    }

    /**
     * 生成适配别名的新条件表达式
     * <p>
     * 用于SQL重写时，将表名替换为别名，保证条件表达式与AST结构一致。
     * </p>
     *
     * @param alias 别名
     * @return 新等值条件表达式
     */
    @Override
    public IConditionExpression reconstructAliasExpression(String alias) {
        return new EqualToConditionExpression(alias, columnName, columnValue);
    }

}
