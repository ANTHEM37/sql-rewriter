package io.github.anthem37.sql.rewiter.core.extension.expression;

import net.sf.jsqlparser.expression.Expression;

/**
 * 条件表达式接口
 *
 * @author anthem37
 * @since 2025/11/12 15:16:11
 */
public interface IConditionExpression extends Expression {

    /**
     * 重新构造别名表达式
     *
     * @param alias 别名
     * @return 重新构造后的条件表达式
     */
    default IConditionExpression reconstructAliasExpression(String alias) {

        return this;
    }
}
