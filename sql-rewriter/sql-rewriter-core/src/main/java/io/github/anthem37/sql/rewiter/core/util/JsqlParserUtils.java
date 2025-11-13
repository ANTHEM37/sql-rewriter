package io.github.anthem37.sql.rewiter.core.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.anthem37.sql.rewiter.core.exception.ErrorEnum;
import io.github.anthem37.sql.rewiter.core.exception.SqlRewriteException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * jsql工具
 *
 * @author anthem37
 * @since 2025/11/12 13:10:46
 */
@Slf4j
public class JsqlParserUtils {


    /**
     * 解析SQL字符串为Statement对象
     *
     * @param sql SQL语句
     * @return Statement对象
     * @throws RuntimeException 解析失败时抛出
     */
    @SneakyThrows
    public static Statement parseSql(String sql) {
        //mp的插件可能会将sql改写后插入较多的/n等符号, 可能会导致CCJSqlParserUtil解析失败
        sql = SqlFormatUtils.cleanSql(sql);
        if (StrUtil.isBlank(sql)) {
            throw new SqlRewriteException(ErrorEnum.SQL_BLANK);
        }
        try {
            return CCJSqlParserUtil.parse(sql);
        } catch (Exception e) {
            throw new SqlRewriteException(e, ErrorEnum.SQL_PARSE_ERROR, sql);
        }
    }

    /**
     * 根据不同类型创建对应的JSQLParser表达式
     *
     * @param value 值
     * @return Expression对象
     * @throws IllegalArgumentException 不支持的类型时抛出
     */
    public static Expression createValueExpression(Object value) {
        if (ObjectUtil.isEmpty(value)) {
            return new NullValue();
        }
        if (value instanceof Long) {
            return new LongValue((Long) value);
        }
        if (value instanceof Double) {
            return new DoubleValue(String.valueOf(value));
        }
        if (value instanceof Date) {
            String dateStr = DateUtil.format((Date) value, DatePattern.NORM_DATE_PATTERN);
            return new StringValue("'" + dateStr + "'");
        }
        if (value instanceof Time) {
            String timeStr = DateUtil.format((Time) value, DatePattern.NORM_TIME_PATTERN);
            return new StringValue("'" + timeStr + "'");
        }
        if (value instanceof Timestamp) {
            String tsStr = DateUtil.format((Timestamp) value, DatePattern.NORM_DATETIME_PATTERN);
            return new TimestampValue(tsStr);
        }
        if (value instanceof DateTime) {
            String dtStr = DateUtil.format((DateTime) value, DatePattern.NORM_DATETIME_PATTERN);
            return new StringValue("'" + dtStr + "'");
        }
        return new StringValue(String.valueOf(value));
    }

    /**
     * 判断表名是否相等（支持别名）
     *
     * @param tableName 目标表名或别名
     * @param table     AST Table对象
     * @return 是否相等
     */
    public static boolean equalToTableName(String tableName, Table table) {
        return equalToTableName(tableName, table, true);
    }

    /**
     * 判断表名是否相等（可选是否使用别名）
     *
     * @param tableName 目标表名或别名
     * @param table     AST Table对象
     * @param useAlias  是否启用别名匹配
     * @return 是否相等
     */
    public static boolean equalToTableName(String tableName, Table table, boolean useAlias) {
        if (StrUtil.isBlank(tableName) || ObjectUtil.isEmpty(table)) {
            return false;
        }
        String normTableName = stripQuotes(tableName);
        String normAstTableName = stripQuotes(table.getName());
        boolean equalTo = StrUtil.equalsIgnoreCase(normTableName, normAstTableName);
        if (!equalTo && useAlias) {
            Alias alias = table.getAlias();
            if (ObjectUtil.isNotEmpty(alias)) {
                String normAlias = stripQuotes(alias.getName());
                equalTo = StrUtil.equalsIgnoreCase(normTableName, normAlias);
            }
        }
        return equalTo;
    }

    /**
     * 去除表名或别名两侧的双引号
     */
    private static String stripQuotes(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    /**
     * 获取表的别名（无别名时返回表名）
     *
     * @param table AST Table对象
     * @return 别名或表名
     */
    public static String getAlias(Table table) {
        if (ObjectUtil.isEmpty(table)) {
            log.warn("尝试获取空Table的别名");
            return null;
        }
        Alias alias = table.getAlias();
        String result;
        if (ObjectUtil.isEmpty(alias)) {
            result = table.getName();
        } else {
            result = alias.getName();
        }
        return result;
    }

}
