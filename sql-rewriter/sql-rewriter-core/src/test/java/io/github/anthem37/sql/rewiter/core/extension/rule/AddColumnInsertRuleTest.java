package io.github.anthem37.sql.rewiter.core.extension.rule;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * AddColumnInsertRule 单元测试
 */
public class AddColumnInsertRuleTest {

    @Test
    public void applyTypedShouldAddColumnWhenTableMatches() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        assertEquals("INSERT INTO tenant (name, tenant_id) VALUES ('NAME', 'TENANT_1')", insert.toString());
        assertSame(insert, statement);
    }

    @Test
    public void applyTypedShouldAppendToRowConstructor() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME'),('OTHER')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", 1L);

        rule.applyTyped(insert);

        assertEquals("INSERT INTO tenant (name, tenant_id) VALUES ('NAME', 1), ('OTHER', 1)", insert.toString());
    }

    @Test
    public void applyTypedShouldHandleFunctionArguments() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES (UPPER('NAME'))");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        assertEquals("INSERT INTO tenant (name, tenant_id) VALUES (UPPER('NAME', 'TENANT_1'))", insert.toString());
    }

    @Test
    public void applyTypedShouldDoNothingWhenTableNotMatch() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO other(name) VALUES ('NAME')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        assertEquals("INSERT INTO other (name) VALUES ('NAME')", insert.toString());
    }
}
