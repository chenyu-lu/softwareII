package com.credit.common.expection;

import com.credit.common.result.Result;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        return Result.fail("系统错误：" + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail("参数验证失败：" + errorMessage);
    }

    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail("参数绑定失败：" + errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.fail("参数错误：" + e.getMessage());
    }
}
