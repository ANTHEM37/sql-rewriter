package io.github.anthem37.sql.rewiter.core.exception;

import lombok.Getter;

/**
 * SQL重写异常
 *
 * @author anthem37
 * @since 2025/11/12 13:46:43
 */
@Getter
public class SqlRewriteException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;


    public SqlRewriteException(ErrorEnum errorEnum, Object... args) {
        super(errorEnum.formatMsg(args));
        this.code = errorEnum.getCode();
    }

    public SqlRewriteException(Throwable cause, ErrorEnum errorEnum, Object... args) {
        super(errorEnum.formatMsg(args), cause);
        this.code = errorEnum.getCode();
    }

    public SqlRewriteException(String code, String message) {
        super(message);
        this.code = code;
    }

    public SqlRewriteException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
