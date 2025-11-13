package io.github.anthem37.sql.rewiter.core.util;

import cn.hutool.core.util.StrUtil;

/**
 * sql格式化工具
 *
 * @author anthem37
 * @since 2025/11/12 13:19:37
 */
public class SqlFormatUtils {

    /**
     * 清理SQL字符串，去除多余换行、空行、首尾空白，将多个空白合并为一个空格
     *
     * @param sql 原始SQL
     * @return 清理后的SQL
     */
    public static String cleanSql(String sql) {
        if (StrUtil.isBlank(sql)) {

            return sql;
        }
        // 1. 去除首尾空白
        String cleaned = sql.trim();
        // 2. 将所有换行、制表符替换为单个空格
        cleaned = cleaned.replaceAll("[\\t\\n\\r]+", StrUtil.SPACE);
        // 3. 多个空格合并为一个空格
        cleaned = cleaned.replaceAll(" +", StrUtil.SPACE);

        return cleaned;
    }
    
}
