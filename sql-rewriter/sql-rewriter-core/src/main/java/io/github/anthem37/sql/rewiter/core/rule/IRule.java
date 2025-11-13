package io.github.anthem37.sql.rewiter.core.rule;

import net.sf.jsqlparser.statement.Statement;

/**
 * SQL重写规则接口s
 *
 * @author anthem37
 * @since 2025/11/12 14:36:38
 */
public interface IRule {

    /**
     * 获取规则优先级
     * 数值越小优先级越高，默认优先级为100
     *
     * @return 优先级数值
     */
    default int getPriority() {
        return RulePriority.DEFAULT;
    }

    /**
     * 判断是否匹配该规则
     *
     * @param statement sql
     * @return 是/否
     */
    boolean match(Statement statement);

    /**
     * 应用规则
     *
     * @param statement sql
     */
    void apply(Statement statement);

}
