package io.github.anthem37.sql.rewiter.core.extension.visitor.impl;

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

public class AddConditionFromItemVisitorTest {

    @Test
    public void visitParenthesedSelectShouldAddConditionInside() throws Exception {
        ParenthesedSelect parenthesedSelect = (ParenthesedSelect) CCJSqlParserUtil.parse("(SELECT * FROM tenant)");
        AddConditionFromItemVisitor visitor = new AddConditionFromItemVisitor(
                "tenant",
                new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1")
        );

        parenthesedSelect.accept(visitor);

        PlainSelect innerSelect = (PlainSelect) parenthesedSelect.getSelect().getSelectBody();
        assertEquals("tenant.tenant_id = 'TENANT_1'", innerSelect.getWhere().toString());
    }

    @Test
    public void visitParenthesedFromItemShouldAddConditionToMatchedTable() throws Exception {
        Statement statement = CCJSqlParserUtil.parse(
                "SELECT * FROM (tenant t LEFT JOIN orders o ON t.id = o.tenant_id)"
        );
        Select select = (Select) statement;
        ParenthesedFromItem parenthesedFromItem = (ParenthesedFromItem) ((PlainSelect) select.getSelectBody()).getFromItem();
        AddConditionFromItemVisitor visitor = new AddConditionFromItemVisitor(
                "orders",
                new EqualToConditionExpression("orders", "tenant_id", "TENANT_1")
        );

        parenthesedFromItem.accept(visitor);

        List<Join> joins = parenthesedFromItem.getJoins();
        assertNotNull(joins);
        assertEquals(1, joins.size());
        Expression onExpression = joins.get(0).getOnExpression();
        assertTrue(onExpression instanceof AndExpression);
        AndExpression andExpression = (AndExpression) onExpression;
        assertEquals("t.id = o.tenant_id", ((Parenthesis) andExpression.getLeftExpression()).getExpression().toString());
        assertEquals("o.tenant_id = 'TENANT_1'", andExpression.getRightExpression().toString());
    }

    @Test
    public void visitParenthesedFromItemShouldNotModifyWhenTableNotMatched() throws Exception {
        Statement statement = CCJSqlParserUtil.parse(
                "SELECT * FROM (tenant t LEFT JOIN orders o ON t.id = o.tenant_id)"
        );
        Select select = (Select) statement;
        ParenthesedFromItem parenthesedFromItem = (ParenthesedFromItem) ((PlainSelect) select.getSelectBody()).getFromItem();
        AddConditionFromItemVisitor visitor = new AddConditionFromItemVisitor(
                "inventory",
                new EqualToConditionExpression("inventory", "tenant_id", "TENANT_1")
        );

        parenthesedFromItem.accept(visitor);

        List<Join> joins = parenthesedFromItem.getJoins();
        assertNotNull(joins);
        assertEquals(1, joins.size());
        assertEquals("t.id = o.tenant_id", joins.get(0).getOnExpression().toString());
    }

    @Test
    public void visitParenthesedFromItemShouldPropagateAliasCondition() throws Exception {
        Statement statement = CCJSqlParserUtil.parse(
                "SELECT * FROM (SELECT * FROM tenant) t"
        );
        Select select = (Select) statement;
        ParenthesedFromItem parenthesedFromItem = (ParenthesedFromItem) ((PlainSelect) select.getSelectBody()).getFromItem();
        AddConditionFromItemVisitor visitor = new AddConditionFromItemVisitor(
                "tenant",
                new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1")
        );

        parenthesedFromItem.accept(visitor);

        ParenthesedSelect innerParenthesedSelect = (ParenthesedSelect) parenthesedFromItem.getFromItem();
        PlainSelect innerSelect = (PlainSelect) innerParenthesedSelect.getSelect().getSelectBody();
        assertEquals("tenant.tenant_id = 'TENANT_1'", innerSelect.getWhere().toString());
    }

    @Test
    public void visitParenthesedFromItemShouldHandleNullJoins() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM (SELECT * FROM tenant)");
        Select select = (Select) statement;
        ParenthesedFromItem parenthesedFromItem = (ParenthesedFromItem) ((PlainSelect) select.getSelectBody()).getFromItem();
        parenthesedFromItem.setJoins(null);
        AddConditionFromItemVisitor visitor = new AddConditionFromItemVisitor(
                "tenant",
                new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1")
        );

        parenthesedFromItem.accept(visitor);

        assertNull(parenthesedFromItem.getJoins());
    }

    @Test
    public void visitParenthesedFromItemShouldHandleNullFromItem() throws Exception {
        ParenthesedFromItem parenthesedFromItem = new ParenthesedFromItem();
        parenthesedFromItem.setFromItem(null);
        AddConditionFromItemVisitor visitor = new AddConditionFromItemVisitor(
                "tenant",
                new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1")
        );

        parenthesedFromItem.accept(visitor);

        assertNull(parenthesedFromItem.getFromItem());
    }
}
