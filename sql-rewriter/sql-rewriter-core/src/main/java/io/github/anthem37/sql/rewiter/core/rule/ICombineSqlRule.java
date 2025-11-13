package io.github.anthem37.sql.rewiter.core.rule;

import java.util.List;

/**
 * 组合SQL规则接口
 *
 * @author anthem37
 * @since 2025/11/13 14:27:30
 */
public interface ICombineSqlRule extends IRule {

    List<ISqlRule<?>> getRules();
}
