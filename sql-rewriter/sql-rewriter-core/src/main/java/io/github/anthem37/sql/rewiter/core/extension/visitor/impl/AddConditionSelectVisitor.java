package io.github.anthem37.sql.rewiter.core.extension.visitor.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import io.github.anthem37.sql.rewiter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewiter.core.extension.visitor.IAddConditionSelectVisitor;
import io.github.anthem37.sql.rewiter.core.util.JsqlParserUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.Optional;

/**
 * Select语句条件访问器实现
 * <p>
 * 用于在SQL AST遍历过程中，针对指定表名的SELECT语句动态添加WHERE或JOIN条件。
 * 支持主表、JOIN表、子查询、嵌套结构等多种情况，自动适配表别名。
 * 典型应用场景：多租户、数据权限、动态条件拼接等。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/12 15:26:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AddConditionSelectVisitor extends SelectVisitorAdapter implements IAddConditionSelectVisitor {

    /**
     * 目标表名（区分大小写，建议与SQL中表名保持一致）
     * 仅当AST节点的表名与此一致时才会应用条件。
     */
    private String tableName;
    /**
     * 条件表达式（如等值、范围等），会自动适配表别名
     * 通过IConditionExpression接口实现，支持灵活扩展。
     */
    private IConditionExpression conditionExpression;

    /**
     * 访问PlainSelect节点，添加where和join条件
     * <p>
     * 1. 若主表匹配目标表名，则在WHERE子句添加条件。
     * 2. 若JOIN表匹配目标表名，则在JOIN ON子句添加条件。
     * 3. 递归处理fromItem（如子查询）、JOIN右表、WHERE表达式中的子查询。
     * 4. 仅支持AST结构递归，不处理SELECT字段列表中的子查询。
     * </p>
     *
     * @param plainSelect select语句AST节点
     */
    @SneakyThrows
    @Override
    public void visit(PlainSelect plainSelect) {
        // 处理主表
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            String alias = JsqlParserUtils.getAlias(table);
            // 主表匹配时，添加条件
            if (JsqlParserUtils.equalToTableName(tableName, table)) {
                IConditionExpression aliasConditionExpression = conditionExpression.reconstructAliasExpression(alias);
                addAndExpression4Where(plainSelect, aliasConditionExpression);
            }
        }

        // 递归处理fromItem（如子查询、嵌套结构）
        AddConditionFromItemVisitor sieveFromItemVisitor = new AddConditionFromItemVisitor(tableName, conditionExpression);
        fromItem.accept(sieveFromItemVisitor);

        // 处理所有JOIN表
        for (Join join : Optional.ofNullable(plainSelect.getJoins()).orElse(Lists.newArrayList())) {
            FromItem rightItem = join.getRightItem();
            if (rightItem instanceof Table) {
                Table table = ((Table) rightItem);
                String alias = JsqlParserUtils.getAlias(table);
                // JOIN表匹配时，添加条件
                if (JsqlParserUtils.equalToTableName(tableName, table)) {
                    IConditionExpression aliasConditionExpression = conditionExpression.reconstructAliasExpression(alias);
                    addAndExpression4Join(join, aliasConditionExpression);
                }
            }
            // 递归处理JOIN右表（如子查询、嵌套结构）
            rightItem.accept(sieveFromItemVisitor);
        }

        // 递归处理WHERE表达式中的子查询（如IN、EXISTS等）
        Expression where = plainSelect.getWhere();
        if (ObjectUtil.isNotEmpty(where)) {
            where.accept(new AddConditionExpressionVisitor(this));
        }
    }
}
