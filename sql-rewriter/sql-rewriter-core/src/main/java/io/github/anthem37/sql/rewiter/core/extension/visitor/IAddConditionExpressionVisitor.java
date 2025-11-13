package io.github.anthem37.sql.rewiter.core.extension.visitor;

import net.sf.jsqlparser.expression.ExpressionVisitor;

/**
 * 添加条件表达式访问器接口
 * <p>
 * 用于在SQL AST中添加新的条件表达式。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/12 15:24:21
 */
public interface IAddConditionExpressionVisitor extends ExpressionVisitor, IAddConditionVisitor {
}
