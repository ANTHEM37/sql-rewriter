package io.github.anthem37.sql.rewiter.core.extension.visitor.impl;

import cn.hutool.core.collection.CollectionUtil;
import io.github.anthem37.sql.rewiter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewiter.core.extension.visitor.IAddConditionFromItemVisitor;
import io.github.anthem37.sql.rewiter.core.util.JsqlParserUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

/**
 * Select FromItem条件递归访问器实现
 * <p>
 * 用于递归遍历SELECT语句的FromItem（如子查询、表、嵌套结构），
 * 动态为目标表添加条件表达式。
 * 典型应用场景：多租户、数据权限等。
 * </p>
 *
 * @author hb28301
 * @since 2025/11/12 15:28:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AddConditionFromItemVisitor extends FromItemVisitorAdapter implements IAddConditionFromItemVisitor {

    /**
     * 目标表名（区分大小写，建议与SQL中表名保持一致）
     */
    private final String tableName;
    /**
     * 条件表达式（如等值、范围等），会自动适配表别名
     */
    private final IConditionExpression conditionExpression;

    /**
     * 访问括号中的SELECT语句，递归添加条件
     *
     * @param selectBody 括号中的SELECT语句
     */
    @Override
    public void visit(ParenthesedSelect selectBody) {
        AddConditionSelectVisitor addConditionSelectVisitor = new AddConditionSelectVisitor(tableName, conditionExpression);
        selectBody.accept(addConditionSelectVisitor);
    }

    @Override
    public void visit(ParenthesedFromItem aThis) {
        FromItem leftFromItem = aThis.getFromItem();
        boolean isAcceptable = false;
        String alias = null;
        if (leftFromItem instanceof Table) {
            Table table = ((Table) leftFromItem);
            isAcceptable = JsqlParserUtils.equalToTableName(tableName, table);
            if (isAcceptable) {
                alias = JsqlParserUtils.getAlias(table);
            }
        }
        leftFromItem.accept(this);
        List<Join> joins = aThis.getJoins();
        if (CollectionUtil.isEmpty(joins)) {
            return;
        }
        for (Join join : joins) {
            FromItem rightItem = join.getRightItem();
            if (!isAcceptable && (rightItem instanceof Table)) {
                Table table = ((Table) rightItem);
                isAcceptable = JsqlParserUtils.equalToTableName(tableName, table);
                if (isAcceptable) {
                    alias = JsqlParserUtils.getAlias(table);
                }
            }
            if (isAcceptable) {
                IConditionExpression aliasConditionExpression = conditionExpression.reconstructAliasExpression(alias);
                addAndExpression4Join(join, aliasConditionExpression);
            }
            rightItem.accept(this);
        }
    }

}

