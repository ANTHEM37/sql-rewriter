package io.github.anthem37.sql.rewiter.core.extension.visitor;

import net.sf.jsqlparser.statement.select.SelectVisitor;

/**
 * 添加条件表达式访问器接口 - 选择语句
 * <p>
 * 用于在SQL AST中添加新的条件表达式。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/12 15:22:49
 */
public interface IAddConditionSelectVisitor extends SelectVisitor, IAddConditionVisitor {
}
