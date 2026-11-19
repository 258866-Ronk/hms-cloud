package com.zcst.hms.common.advice;

import com.zcst.hms.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：统一返回 Result 结构
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Object> handleIllegalArgument(IllegalArgumentException e) {
        return Result.fail(400, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Object> handleRuntime(RuntimeException e) {
        return Result.fail(500, e.getMessage() == null ? "服务内部异常" : e.getMessage());
    }
}
