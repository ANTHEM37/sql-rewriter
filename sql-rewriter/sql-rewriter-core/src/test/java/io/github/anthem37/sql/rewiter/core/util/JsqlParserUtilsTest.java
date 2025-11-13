package io.github.anthem37.sql.rewiter.core.util;

import io.github.anthem37.sql.rewiter.core.exception.ErrorEnum;
import io.github.anthem37.sql.rewiter.core.exception.SqlRewriteException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static org.junit.Assert.*;

public class JsqlParserUtilsTest {

    @Test
    public void parseSqlShouldTrimAndParseValidSql() {
        Statement statement = JsqlParserUtils.parseSql("   SELECT * FROM tenant   \n\t");

        assertNotNull(statement);
        assertEquals("SELECT * FROM tenant", statement.toString());
    }

    @Test
    public void parseSqlShouldThrowExceptionWhenSqlIsBlank() {
        SqlRewriteException exception = assertThrows(SqlRewriteException.class, () -> JsqlParserUtils.parseSql("   "));

        assertEquals(ErrorEnum.SQL_BLANK.getErrorMsg(), exception.getMessage());
    }

    @Test
    public void parseSqlShouldThrowExceptionWhenParseFails() {
        SqlRewriteException exception = assertThrows(SqlRewriteException.class, () -> JsqlParserUtils.parseSql("INVALID"));

        assertTrue(exception.getMessage().contains(ErrorEnum.SQL_PARSE_ERROR.getErrorMsg().replace("{}", "INVALID")));
        assertEquals(ErrorEnum.SQL_PARSE_ERROR.getCode(), exception.getCode());
    }

    @Test
    public void createValueExpressionShouldHandleNull() {
        Expression expression = JsqlParserUtils.createValueExpression(null);

        assertTrue(expression instanceof NullValue);
    }

    @Test
    public void createValueExpressionShouldHandleLong() {
        Expression expression = JsqlParserUtils.createValueExpression(123L);

        assertTrue(expression instanceof LongValue);
        assertEquals("123", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleDate() {
        Date date = Date.valueOf("2025-11-13");

        Expression expression = JsqlParserUtils.createValueExpression(date);

        assertTrue(expression instanceof StringValue);
        assertEquals("'2025-11-13'", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleTime() {
        Time time = Time.valueOf("10:15:30");

        Expression expression = JsqlParserUtils.createValueExpression(time);

        assertEquals("'10:15:30'", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleTimestamp() {
        Timestamp timestamp = Timestamp.valueOf("2025-11-13 10:15:30");

        Expression expression = JsqlParserUtils.createValueExpression(timestamp);

        assertEquals("2025-11-13 10:15:30", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleString() {
        Expression expression = JsqlParserUtils.createValueExpression("TENANT");

        assertTrue(expression instanceof StringValue);
        assertEquals("TENANT", expression.toString());
    }

    @Test
    public void equalToTableNameShouldMatchNameAndAliasCaseInsensitive() {
        Table table = new Table("Tenant");
        table.setAlias(new net.sf.jsqlparser.expression.Alias("t"));

        assertTrue(JsqlParserUtils.equalToTableName("tenant", table));
        assertTrue(JsqlParserUtils.equalToTableName("t", table));
        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant\"", table));
    }

    @Test
    public void equalToTableNameShouldRespectAliasFlag() {
        Table table = new Table("Tenant");
        table.setAlias(new net.sf.jsqlparser.expression.Alias("t"));

        assertTrue(JsqlParserUtils.equalToTableName("tenant", table, false));
        assertFalse(JsqlParserUtils.equalToTableName("t", table, false));
    }

    @Test
    public void getAliasShouldReturnAliasWhenPresent() {
        Table table = new Table("Tenant");
        table.setAlias(new net.sf.jsqlparser.expression.Alias("t"));

        assertEquals("t", JsqlParserUtils.getAlias(table));
    }

    @Test
    public void getAliasShouldReturnTableNameWhenAliasMissing() {
        Table table = new Table("Tenant");

        assertEquals("Tenant", JsqlParserUtils.getAlias(table));
    }
}
