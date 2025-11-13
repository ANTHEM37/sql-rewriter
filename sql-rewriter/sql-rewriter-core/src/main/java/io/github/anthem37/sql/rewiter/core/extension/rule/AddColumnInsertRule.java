package io.github.anthem37.sql.rewiter.core.extension.rule;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import io.github.anthem37.sql.rewiter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewiter.core.rule.RulePriority;
import io.github.anthem37.sql.rewiter.core.util.JsqlParserUtils;
import lombok.Getter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Values;

import java.util.List;

/**
 * Insert语句添加列规则实现
 * <p>
 * 用于在SQL重写过程中，针对指定表名的INSERT语句动态添加新列及其值。
 * 支持批量插入、无列名插入等多种场景。
 * 典型应用场景：多租户自动补充租户字段、数据审计等。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/13 14:29:01
 */
@Getter
public class AddColumnInsertRule implements ISqlRule<Insert> {

    /**
     * 目标表名（区分大小写，建议与SQL中表名保持一致）
     * 仅当SQL语句的表名与此一致时才会应用本规则。
     */
    private final String tableName;

    /**
     * 新增列名（如tenant_id、created_by等）
     */
    private final String columnName;

    /**
     * 新增列的值（支持任意类型，最终会转为SQL表达式）
     */
    private final Object columnValue;

    /**
     * 规则优先级，数值越小优先级越高
     * 便于多规则协同时控制应用顺序。
     */
    private final int priority;

    /**
     * 构造函数，使用默认优先级（RulePriority.INSERT_DEFAULT）
     *
     * @param tableName   目标表名
     * @param columnName  新增列名
     * @param columnValue 新增列的值
     */
    public AddColumnInsertRule(String tableName, String columnName, Object columnValue) {
        this(tableName, columnName, columnValue, RulePriority.INSERT_DEFAULT);
    }

    /**
     * 构造函数，支持自定义优先级
     *
     * @param tableName   目标表名
     * @param columnName  新增列名
     * @param columnValue 新增列的值
     * @param priority    规则优先级
     */
    public AddColumnInsertRule(String tableName, String columnName, Object columnValue, int priority) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnValue = columnValue;
        this.priority = priority;
    }

    /**
     * 获取本规则适用的SQL类型（Insert）
     *
     * @return Insert.class
     */
    @Override
    public Class<Insert> getType() {
        return Insert.class;
    }

    /**
     * 获取目标表名
     *
     * @return 目标表名
     */
    @Override
    public String getTargetTableName() {
        return tableName;
    }

    /**
     * 获取规则优先级
     *
     * @return 优先级数值
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * 应用规则到Insert语句
     * <p>
     * 仅当Insert语句的表名与目标表名一致时，才会添加新列及其值。
     * 支持有列名、无列名、批量插入等多种结构。
     * </p>
     *
     * @param insert 需要重写的Insert对象
     */
    @Override
    public void applyTyped(Insert insert) {
        Table table = insert.getTable();
        if (!matchTable(table)) {
            return;
        }

        // 添加新列名
        insert.addColumns(new Column(columnName));

        Values values = insert.getValues();
        // 添加新值
        ExpressionList insertItemsList = values.getExpressions();
        List<Expression> insertItemsListExpressions = Lists.newArrayList(insertItemsList.getExpressions());
        if (CollectionUtil.isEmpty(insertItemsListExpressions)) {
            insertItemsListExpressions.add(JsqlParserUtils.createValueExpression(columnValue));
            insertItemsList.setExpressions(insertItemsListExpressions);
            return;
        }
        Expression firstExpression = insertItemsListExpressions.get(0);
        if (firstExpression instanceof RowConstructor) {
            // 处理RowConstructor类型
            for (Expression expression : insertItemsListExpressions) {
                RowConstructor rowConstructor = (RowConstructor) expression;
                rowConstructor.getExpressions().add(JsqlParserUtils.createValueExpression(columnValue));
            }
            return;
        }
        if (firstExpression instanceof Function) {
            // 处理Function类型
            for (Expression expression : insertItemsListExpressions) {
                Function function = (Function) expression;
                ExpressionList parameters = function.getParameters();
                parameters.getExpressions().add(JsqlParserUtils.createValueExpression(columnValue));
            }
            return;
        }
        // 处理普通值列表
        insertItemsListExpressions.add(JsqlParserUtils.createValueExpression(columnValue));
        insertItemsList.setExpressions(insertItemsListExpressions);
    }

}

