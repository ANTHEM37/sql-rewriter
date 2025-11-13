package io.github.anthem37.sql.rewiter.core.rule;

/**
 * 规则优先级
 *
 * @author anthem37
 * @since 2025/11/12 14:37:54
 */
public final class RulePriority {

    /**
     * 最高优先级 - 系统级规则
     */
    public static final int HIGHEST = 1;

    /**
     * 高优先级 - 安全相关规则
     */
    public static final int HIGH = 5;

    /**
     * 中等优先级 - 业务逻辑规则
     */
    public static final int MEDIUM = 10;

    /**
     * 低优先级 - 数据转换规则
     */
    public static final int LOW = 20;

    /**
     * 最低优先级 - 日志记录规则
     */
    public static final int LOWEST = 50;

    /**
     * 默认优先级
     */
    public static final int DEFAULT = 100;

    /**
     * Select语句规则默认优先级
     */
    public static final int SELECT_DEFAULT = 10;

    /**
     * Insert语句规则默认优先级
     */
    public static final int INSERT_DEFAULT = 20;

    /**
     * Update语句规则默认优先级
     */
    public static final int UPDATE_DEFAULT = 30;

    /**
     * Delete语句规则默认优先级
     */
    public static final int DELETE_DEFAULT = 40;

    private RulePriority() {
        // 工具类，禁止实例化
    }
}
