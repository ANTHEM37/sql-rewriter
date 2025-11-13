package io.github.anthem37.sql.rewiter.core.exception;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误枚举
 *
 * @author anthem37
 * @since 2025/11/12 13:20:37
 */
@Getter
@AllArgsConstructor
public enum ErrorEnum {
    SQL_BLANK("sql_blank", "SQL语句为空"),
    SQL_PARSE_ERROR("sql_parse_error", "SQL解析异常: {}"),
    SQL_REWRITE_ERROR("sql_rewrite_error", "SQL重写异常: {}");

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误信息
     */
    private final String errorMsg;

    public String formatMsg(Object... args) {
        return StrUtil.format(errorMsg, args);
    }

}
