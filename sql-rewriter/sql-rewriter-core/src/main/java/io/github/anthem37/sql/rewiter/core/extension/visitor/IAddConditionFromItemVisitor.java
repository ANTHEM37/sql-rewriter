package io.github.anthem37.sql.rewiter.core.extension.visitor;

import net.sf.jsqlparser.statement.select.FromItemVisitor;

/**
 * 添加条件表达式访问器接口 - 从项访问器
 * <p>
 * 用于在SQL AST中添加新的条件表达式。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/12 15:23:43
 */
public interface IAddConditionFromItemVisitor extends FromItemVisitor, IAddConditionVisitor {
}
