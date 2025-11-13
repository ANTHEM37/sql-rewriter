package io.github.anthem37.sql.rewiter.core.extension.rule;

import io.github.anthem37.sql.rewiter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewiter.core.extension.visitor.impl.AddConditionSelectVisitor;
import io.github.anthem37.sql.rewiter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewiter.core.rule.RulePriority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.sf.jsqlparser.statement.select.Select;

/**
 * 为Select语句添加条件的规则实现
 * <p>
 * 用于在SQL重写过程中，针对指定表名的SELECT语句动态添加WHERE条件。
 * 支持优先级配置，便于与其他规则协同工作。
 * 典型应用场景：多租户、数据权限等。
 *
 * @author hb28301
 * @since 2025/11/13 14:24:43
 */
@Getter
@AllArgsConstructor
public class AddConditionSelectRule implements ISqlRule<Select> {

    /**
     * 目标表名（区分大小写，建议与SQL中表名保持一致）
     * 仅当SQL语句的主表或JOIN表名与此一致时才会应用本规则。
     */
    private final String tableName;

    /**
     * 条件表达式（如等值、范围等），会自动适配表别名
     * 通过IConditionExpression接口实现，支持灵活扩展。
     */
    private final IConditionExpression conditionExpression;

    /**
     * 规则优先级，数值越小优先级越高
     * 便于多规则协同时控制应用顺序。
     */
    private final int priority;

    /**
     * 构造函数，使用默认优先级（RulePriority.SELECT_DEFAULT）
     *
     * @param tableName           目标表名
     * @param conditionExpression 条件表达式
     */
    public AddConditionSelectRule(String tableName, IConditionExpression conditionExpression) {
        this(tableName, conditionExpression, RulePriority.SELECT_DEFAULT);
    }

    /**
     * 获取本规则适用的SQL类型（Select）
     *
     * @return Select.class
     */
    @Override
    public Class<Select> getType() {
        return Select.class;
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
     * 应用规则到Select语句
     * <p>
     * 仅当Select语句的主表或JOIN表名与目标表名一致时，才会通过visitor添加条件。
     * </p>
     *
     * @param select 需要重写的Select对象
     */
    @Override
    public void applyTyped(Select select) {
        // 只处理目标表
        AddConditionSelectVisitor selectVisitor = new AddConditionSelectVisitor(tableName, conditionExpression);
        select.accept(selectVisitor);
    }

}

