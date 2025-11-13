package io.github.anthem37.sql.rewiter.core.rule;

import io.github.anthem37.sql.rewiter.core.util.JsqlParserUtils;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * sql规则接口
 *
 * @author anthem37
 * @since 2025/11/13 14:23:20
 */
public interface ISqlRule<T extends Statement> extends IRule {

    /**
     * 获取支持的SQL类型
     */
    Class<T> getType();

    /**
     * 需要匹配的表名，若为null则不做表名过滤
     */
    default String getTargetTableName() {

        return null;
    }

    /**
     * 表名是否匹配
     */
    default boolean matchTable(Table table) {

        return JsqlParserUtils.equalToTableName(getTargetTableName(), table);
    }

    /**
     * 应用规则到具体类型
     */
    void applyTyped(T statement);

    /**
     * 获取规则优先级
     * 根据SQL类型提供不同的默认优先级：
     * - Select: SELECT_DEFAULT (查询优先级最高)
     * - Insert: INSERT_DEFAULT (插入优先级较高)
     * - Update: UPDATE_DEFAULT (更新优先级中等)
     * - Delete: DELETE_DEFAULT (删除优先级较低)
     * - 其他: DEFAULT (默认优先级)
     */
    @Override
    default int getPriority() {
        Class<T> type = getType();
        if (Select.class.isAssignableFrom(type)) {

            return RulePriority.SELECT_DEFAULT;
        }
        if (Insert.class.isAssignableFrom(type)) {

            return RulePriority.INSERT_DEFAULT;
        }
        if (Update.class.isAssignableFrom(type)) {

            return RulePriority.UPDATE_DEFAULT;
        }
        if (Delete.class.isAssignableFrom(type)) {

            return RulePriority.DELETE_DEFAULT;
        }

        return RulePriority.DEFAULT;
    }

    @Override
    default boolean match(Statement statement) {

        return getType().isInstance(statement);
    }

    @Override
    @SuppressWarnings("unchecked")
    default void apply(Statement statement) {
        if (match(statement)) {
            applyTyped((T) statement);
        }
    }

}