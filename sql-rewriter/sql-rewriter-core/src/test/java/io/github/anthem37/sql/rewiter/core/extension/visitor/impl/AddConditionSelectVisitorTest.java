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

public class AddConditionSelectVisitorTest {

    @Test
    public void visitShouldAppendConditionToMainTable() throws Exception {
        Select select = parseSelect("SELECT * FROM tenant");
        AddConditionSelectVisitor visitor = new AddConditionSelectVisitor(
                "tenant",
                new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1")
        );

        select.accept(visitor);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void visitShouldAppendConditionToJoinTable() throws Exception {
        Select select = parseSelect("SELECT * FROM tenant t JOIN orders o ON t.id = o.tenant_id");
        AddConditionSelectVisitor visitor = new AddConditionSelectVisitor(
                "orders",
                new EqualToConditionExpression("orders", "tenant_id", "TENANT_1")
        );

        select.accept(visitor);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        assertNotNull(joins);
        assertEquals(1, joins.size());
        Expression onExpression = joins.get(0).getOnExpression();
        assertTrue(onExpression instanceof AndExpression);
        AndExpression andExpression = (AndExpression) onExpression;
        assertEquals("t.id = o.tenant_id", ((Parenthesis) andExpression.getLeftExpression()).getExpression().toString());
        assertEquals("o.tenant_id = 'TENANT_1'", andExpression.getRightExpression().toString());
    }

    @Test
    public void visitShouldTraverseNestedParenthesedSelect() throws Exception {
        Select select = parseSelect("SELECT * FROM (SELECT * FROM tenant) t");
        AddConditionSelectVisitor visitor = new AddConditionSelectVisitor(
                "tenant",
                new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1")
        );

        select.accept(visitor);

        PlainSelect outerSelect = (PlainSelect) select.getSelectBody();
        FromItem fromItem = outerSelect.getFromItem();
        ParenthesedSelect parenthesedSelect = (ParenthesedSelect) fromItem;
        PlainSelect innerSelect = (PlainSelect) parenthesedSelect.getSelect().getSelectBody();
        assertEquals("tenant.tenant_id = 'TENANT_1'", innerSelect.getWhere().toString());
    }

    @Test
    public void visitShouldHandleParenthesedFromItemJoins() throws Exception {
        Select select = parseSelect(
                "SELECT * FROM (tenant t LEFT JOIN orders o ON t.id = o.tenant_id)"
        );
        AddConditionSelectVisitor visitor = new AddConditionSelectVisitor(
                "orders",
                new EqualToConditionExpression("orders", "tenant_id", "TENANT_1")
        );

        select.accept(visitor);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        ParenthesedFromItem parenthesedFromItem = (ParenthesedFromItem) plainSelect.getFromItem();
        List<Join> joins = parenthesedFromItem.getJoins();
        assertNotNull(joins);
        assertEquals(1, joins.size());
        AndExpression onExpression = (AndExpression) joins.get(0).getOnExpression();
        assertEquals("t.id = o.tenant_id", ((Parenthesis) onExpression.getLeftExpression()).getExpression().toString());
        assertEquals("o.tenant_id = 'TENANT_1'", onExpression.getRightExpression().toString());
    }

    @Test
    public void visitShouldTraverseWhereSubSelect() throws Exception {
        Select select = parseSelect(
                "SELECT * FROM tenant WHERE EXISTS (SELECT 1 FROM orders o WHERE o.id = tenant.id)"
        );
        AddConditionSelectVisitor visitor = new AddConditionSelectVisitor(
                "orders",
                new EqualToConditionExpression("orders", "tenant_id", "TENANT_1")
        );

        select.accept(visitor);

        String rewrittenSql = select.toString();
        assertTrue(rewrittenSql.contains("o.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void visitShouldNotModifyWhenTableNotMatched() throws Exception {
        String originalSql = "SELECT * FROM tenant";
        Select select = parseSelect(originalSql);
        AddConditionSelectVisitor visitor = new AddConditionSelectVisitor(
                "orders",
                new EqualToConditionExpression("orders", "tenant_id", "TENANT_1")
        );

        select.accept(visitor);

        assertEquals(originalSql, select.toString());
    }

    @Test
    public void visitShouldHandleNullFromItemGracefully() throws Exception {
        Select select = parseSelect("SELECT 1");
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        plainSelect.setFromItem(null);
        AddConditionSelectVisitor visitor = new AddConditionSelectVisitor(
                "tenant",
                new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1")
        );

        select.accept(visitor);

        assertNull(plainSelect.getWhere());
    }

    private Select parseSelect(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        return (Select) statement;
    }
}
