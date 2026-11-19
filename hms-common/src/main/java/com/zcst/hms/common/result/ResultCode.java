package com.zcst.hms.common.result;

/**
 * 统一响应状态码
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器内部错误"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或令牌无效"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    NO_SOURCE(601, "号源不足，挂号失败"),
    DUPLICATE_REG(602, "请勿重复挂号"),
    SERVICE_DEGRADED(603, "服务繁忙，已触发熔断降级");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
