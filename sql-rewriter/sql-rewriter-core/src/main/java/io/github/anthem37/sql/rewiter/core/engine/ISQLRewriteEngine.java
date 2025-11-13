package io.github.anthem37.sql.rewiter.core.engine;

import io.github.anthem37.sql.rewiter.core.rule.IRule;

import java.util.List;

/**
 * SQL重写引擎接口
 *
 * @author anthem37
 * @since 2025/11/12 14:48:22
 */
public interface ISQLRewriteEngine {

    /**
     * 获取sql改写规则
     *
     * @return sql改写规则
     */
    List<IRule> getRules();

    /**
     * 执行sql改写
     *
     * @param sql sql
     * @return 改写后的sql
     */
    String run(String sql);

}
