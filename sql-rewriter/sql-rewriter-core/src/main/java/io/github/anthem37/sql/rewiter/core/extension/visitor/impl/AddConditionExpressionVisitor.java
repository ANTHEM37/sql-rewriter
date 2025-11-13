package io.github.anthem37.sql.rewiter.core.extension.visitor.impl;

import io.github.anthem37.sql.rewiter.core.extension.visitor.IAddConditionExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.select.SelectVisitor;

/**
 * 添加条件表达式访问器实现类
 * <p>
 * 用于在SQL AST中添加新的条件表达式。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/12 15:28:08
 */
public class AddConditionExpressionVisitor extends ExpressionVisitorAdapter implements IAddConditionExpressionVisitor {

    /**
     * 构造方法，设置SelectVisitor
     *
     * @param selectVisitor select访问器
     */
    public AddConditionExpressionVisitor(SelectVisitor selectVisitor) {
        setSelectVisitor(selectVisitor);
    }

}