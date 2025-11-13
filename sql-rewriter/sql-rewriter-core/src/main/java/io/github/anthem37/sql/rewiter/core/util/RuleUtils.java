package io.github.anthem37.sql.rewiter.core.util;

import io.github.anthem37.sql.rewiter.core.rule.IRule;
import io.github.anthem37.sql.rewiter.core.rule.ISqlRule;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则工具类，提供优先级排序等通用方法
 *
 * @author anthem37
 * @since 2025/11/12 14:51:41
 */
public class RuleUtils {

    /**
     * 按优先级排序IRule列表
     *
     * @param rules 规则列表
     * @return 按优先级排序后的规则列表
     */
    public static <T extends IRule> List<T> sortByPriority(List<T> rules) {

        return rules == null ? Collections.emptyList() : rules.stream().sorted(Comparator.comparingInt(IRule::getPriority)).collect(Collectors.toList());
    }

    /**
     * 按优先级排序ISqlRule列表
     */
    public static <T extends ISqlRule<?>> List<T> sortSqlRulesByPriority(List<T> rules) {

        return rules == null ? Collections.emptyList() : rules.stream().sorted(Comparator.comparingInt(ISqlRule::getPriority)).collect(Collectors.toList());
    }
}
