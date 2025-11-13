package io.github.anthem37.sql.rewiter.core.engine.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import io.github.anthem37.sql.rewiter.core.engine.ISQLRewriteEngine;
import io.github.anthem37.sql.rewiter.core.rule.IRule;
import io.github.anthem37.sql.rewiter.core.util.GsonUtils;
import io.github.anthem37.sql.rewiter.core.util.JsqlParserUtils;
import io.github.anthem37.sql.rewiter.core.util.RuleUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;

import java.util.Collections;
import java.util.List;

/**
 * SQL重写引擎实现类
 *
 * @author anthem37
 * @since 2025/11/12 14:49:12
 */
@Slf4j
@Getter
public class SQLRewriteEngine implements ISQLRewriteEngine {

    private final List<IRule> rules;

    public SQLRewriteEngine(List<IRule> rules) {
        String rulesStr = GsonUtils.toJson(rules, true);
        // 构造时排序并封装为不可变列表
        if (CollectionUtil.isEmpty(rules)) {
            this.rules = Collections.emptyList();
            log.debug("构造SQLRewriteEngine,SQL重写引擎初始化完成，规则列表为空");
            return;
        }
        this.rules = Collections.unmodifiableList(RuleUtils.sortByPriority(rules));
        log.debug("构造SQLRewriteEngine, SQL重写引擎初始化完成，规则数量: {}, 规则列表: {}", rules.size(), rulesStr);
    }

    @Override
    public String run(String sql) {
        if (CollectionUtil.isEmpty(rules)) {
            log.debug("SQLRewriteEngine.run, 规则列表为空，直接返回原SQL: {}", sql);
            return sql;
        }
        long start = System.currentTimeMillis();
        Statement statement;
        try {
            statement = JsqlParserUtils.parseSql(sql);
        } catch (Exception e) {
            log.warn("SQLRewriteEngine.run, SQL不支持重写，直接返回原SQL: \nSQL: {} \n异常: {}", sql, ExceptionUtil.stacktraceToString(e));

            return sql;
        }
        try {
            String beforeSql = statement.toString();
            for (IRule rule : rules) {
                if (rule.match(statement)) {
                    rule.apply(statement);
                }
            }
            String result = statement.toString();
            if (StrUtil.equals(beforeSql, result)) {
                log.debug("SQLRewriteEngine.run, SQL无需重写，直接返回原SQL: {}", sql);

                return sql;
            }
            long cost = System.currentTimeMillis() - start;
            log.debug("SQLRewriteEngine.run, SQL重写耗时: {} ms, \n原始SQL: {}, \n重写后SQL: {}", cost, sql, result);
            return result;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("SQLRewriteEngine.run, SQL重写失败，耗时: {} ms, \n原始SQL: {}, \n错误信息: {}", cost, sql, e.getMessage(), e);
            return sql;
        }
    }
}
