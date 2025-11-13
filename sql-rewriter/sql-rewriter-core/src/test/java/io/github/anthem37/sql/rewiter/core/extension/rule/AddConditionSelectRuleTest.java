package io.github.anthem37.sql.rewiter.core.extension.rule;

import io.github.anthem37.sql.rewiter.core.extension.expression.impl.EqualToConditionExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * AddConditionSelectRule 单元测试
 */
public class AddConditionSelectRuleTest {

    @Test
    public void applyTypedShouldAppendConditionToWhereClauseWhenTableMatches() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));

        rule.applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where = plainSelect.getWhere();
        assertNotNull(where);
        assertEquals("tenant.tenant_id = 'TENANT_1'", where.toString());
    }

    @Test
    public void applyTypedShouldMergeWithExistingWhereClause() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant WHERE tenant.status = 'ACTIVE'");
        Select select = (Select) statement;
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));

        rule.applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        AndExpression where = (AndExpression) plainSelect.getWhere();
        assertTrue(where.getLeftExpression() instanceof Parenthesis);
        assertEquals("tenant.status = 'ACTIVE'", ((Parenthesis) where.getLeftExpression()).getExpression().toString());
        assertEquals("tenant.tenant_id = 'TENANT_1'", where.getRightExpression().toString());
    }

    @Test
    public void applyTypedShouldAppendConditionToJoin() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t JOIN orders o ON t.id = o.tenant_id");
        Select select = (Select) statement;
        AddConditionSelectRule rule = new AddConditionSelectRule("orders", new EqualToConditionExpression("orders", "tenant_id", "TENANT_1"));

        rule.applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        assertNotNull(joins);
        assertEquals(1, joins.size());
        AndExpression onExpression = (AndExpression) joins.get(0).getOnExpression();
        assertEquals("t.id = o.tenant_id", ((Parenthesis) onExpression.getLeftExpression()).getExpression().toString());
        assertEquals("o.tenant_id = 'TENANT_1'", onExpression.getRightExpression().toString());
    }

    @Test
    public void applyTypedShouldPropagateIntoNestedSelect() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM (SELECT * FROM tenant) t");
        Select select = (Select) statement;
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));

        rule.applyTyped(select);

        PlainSelect outerSelect = (PlainSelect) select.getSelectBody();
        FromItem fromItem = outerSelect.getFromItem();
        ParenthesedSelect parenthesedSelect = (ParenthesedSelect) fromItem;
        PlainSelect innerSelect = (PlainSelect) parenthesedSelect.getSelect().getSelectBody();
        assertEquals("tenant.tenant_id = 'TENANT_1'", innerSelect.getWhere().toString());
    }

    @Test
    public void applyTypedShouldTraverseWhereSubSelects() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant WHERE EXISTS (SELECT 1 FROM orders o WHERE o.id = tenant.id)");
        Select select = (Select) statement;
        AddConditionSelectRule rule = new AddConditionSelectRule("orders", new EqualToConditionExpression("orders", "tenant_id", "TENANT_1"));

        rule.applyTyped(select);

        String rewrittenSql = select.toString();
        assertTrue(rewrittenSql.contains("o.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void applyTypedShouldLeaveSqlUntouchedWhenTableDoesNotMatch() throws Exception {
        String originalSql = "SELECT * FROM orders";
        Statement statement = CCJSqlParserUtil.parse(originalSql);
        Select select = (Select) statement;
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));

        rule.applyTyped(select);

        assertEquals(originalSql, select.toString());
    }

    @Test
    public void getTypeShouldReturnSelectClass() {
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));

        assertSame(Select.class, rule.getType());
    }

    @Test
    public void getPriorityShouldReturnConfiguredValue() {
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"), 3);

        assertEquals(3, rule.getPriority());
    }

    @Test
    public void getTargetTableNameShouldReturnConfiguredName() {
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));

        assertEquals("tenant", rule.getTargetTableName());
    }
}
