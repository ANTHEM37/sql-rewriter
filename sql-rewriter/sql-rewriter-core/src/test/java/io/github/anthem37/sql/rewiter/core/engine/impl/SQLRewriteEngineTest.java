package io.github.anthem37.sql.rewiter.core.engine.impl;

import io.github.anthem37.sql.rewiter.core.rule.IRule;
import io.github.anthem37.sql.rewiter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewiter.core.rule.RulePriority;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SQLRewriteEngine 核心行为单元测试
 */
public class SQLRewriteEngineTest {

    @Test
    public void runShouldReturnOriginalSqlWhenRulesEmpty() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.emptyList());
        String originalSql = "SELECT * FROM tenant";

        String result = engine.run(originalSql);

        assertEquals(originalSql, result);
    }

    @Test
    public void runShouldApplyMatchingRule() {
        TrackingSelectRule rule = new TrackingSelectRule("tenant");
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.singletonList(rule));

        String result = engine.run("SELECT * FROM tenant");

        assertEquals("SELECT * FROM tenant WHERE tenant.tenant_id = 'TENANT_1'", result);
        assertTrue(rule.wasApplied());
    }

    @Test
    public void runShouldApplyAllMatchingRulesInPriorityOrder() {
        TrackingSelectRule highPriorityRule = new TrackingSelectRule("tenant", RulePriority.HIGH, "TENANT_3");
        TrackingSelectRule mediumPriorityRule = new TrackingSelectRule("tenant", RulePriority.MEDIUM, "TENANT_2");
        TrackingSelectRule lowPriorityRule = new TrackingSelectRule("tenant", RulePriority.LOW, "TENANT_1");
        SQLRewriteEngine engine = new SQLRewriteEngine(Arrays.asList(lowPriorityRule, mediumPriorityRule, highPriorityRule));

        String result = engine.run("SELECT * FROM tenant");

        assertEquals("SELECT * FROM tenant WHERE tenant.tenant_id = 'TENANT_1' AND (tenant.tenant_id = 'TENANT_2') AND (tenant.tenant_id = 'TENANT_3')", result);
        assertEquals(Arrays.asList(highPriorityRule, mediumPriorityRule, lowPriorityRule), new ArrayList<>(engine.getRules()));
        assertTrue(highPriorityRule.wasApplied());
        assertTrue(mediumPriorityRule.wasApplied());
        assertTrue(lowPriorityRule.wasApplied());
    }

    @Test
    public void runShouldReturnOriginalSqlWhenRuleThrowsException() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.singletonList(new ThrowingSelectRule()));
        String originalSql = "SELECT * FROM tenant";

        String result = engine.run(originalSql);

        assertEquals(originalSql, result);
    }

    @Test
    public void runShouldReturnOriginalSqlWhenParseFails() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.emptyList());
        String invalidSql = "INVALID SQL";

        String result = engine.run(invalidSql);

        assertEquals(invalidSql, result);
    }

    @Test
    public void constructorShouldSortRulesByPriority() {
        TrackingSelectRule lowPriority = new TrackingSelectRule("tenant", RulePriority.LOWEST);
        TrackingSelectRule highPriority = new TrackingSelectRule("tenant", RulePriority.HIGHEST);
        List<ISqlRule<Select>> rules = Arrays.asList(lowPriority, highPriority);

        SQLRewriteEngine engine = new SQLRewriteEngine(new ArrayList<>(rules));

        List<? extends IRule> sortedRules = engine.getRules();
        assertSame(highPriority, sortedRules.get(0));
        assertSame(lowPriority, sortedRules.get(1));
    }

    private static final class TrackingSelectRule implements ISqlRule<Select> {

        private final String tableName;
        private final int priority;
        private final String appendedSuffix;
        private boolean applied;

        private TrackingSelectRule(String tableName) {
            this(tableName, RulePriority.SELECT_DEFAULT, "TENANT_1");
        }

        private TrackingSelectRule(String tableName, int priority) {
            this(tableName, priority, "TENANT_1");
        }

        private TrackingSelectRule(String tableName, int priority, String appendedSuffix) {
            this.tableName = tableName;
            this.priority = priority;
            this.appendedSuffix = appendedSuffix;
        }

        @Override
        public Class<Select> getType() {
            return Select.class;
        }

        @Override
        public String getTargetTableName() {
            return tableName;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public void applyTyped(Select statement) {
            PlainSelect plainSelect = (PlainSelect) statement.getSelectBody();
            Table table = (Table) plainSelect.getFromItem();
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(new Column(new Table(table.getName()), "tenant_id"));
            equalsTo.setRightExpression(new StringValue("'" + appendedSuffix + "'"));
            plainSelect.setWhere(equalsTo);
            applied = true;
        }

        private boolean wasApplied() {
            return applied;
        }
    }

    private static final class ThrowingSelectRule implements ISqlRule<Select> {

        @Override
        public Class<Select> getType() {
            return Select.class;
        }

        @Override
        public void applyTyped(Select statement) {
            throw new IllegalStateException("rule failed");
        }
    }
}
