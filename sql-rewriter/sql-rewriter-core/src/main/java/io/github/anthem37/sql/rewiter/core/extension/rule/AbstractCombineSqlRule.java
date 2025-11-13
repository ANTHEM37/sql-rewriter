package io.github.anthem37.sql.rewiter.core.extension.rule;

import io.github.anthem37.sql.rewiter.core.rule.ICombineSqlRule;
import io.github.anthem37.sql.rewiter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewiter.core.util.RuleUtils;
import lombok.Getter;
import net.sf.jsqlparser.statement.Statement;

import java.util.Collections;
import java.util.List;

/**
 * 组合SQL规则抽象类
 *
 * @author anthem37
 * @since 2025/11/13 14:26:51
 */
@Getter
public abstract class AbstractCombineSqlRule implements ICombineSqlRule {

    private final List<ISqlRule<?>> rules;

    public AbstractCombineSqlRule(List<ISqlRule<?>> rules) {
        this.rules = Collections.unmodifiableList(RuleUtils.sortSqlRulesByPriority(rules));
    }

    @Override
    public boolean match(Statement statement) {

        return rules.stream().anyMatch(sqlRule -> sqlRule.match(statement));
    }

    @Override
    public void apply(Statement statement) {
        for (ISqlRule<?> rule : rules) {
            rule.apply(statement);
        }
    }

}
