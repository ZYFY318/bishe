package com.example.houduan.exception;

import com.example.houduan.pojo.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class GlobalExceptionHandlerAdvice {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandlerAdvice.class);

    // 处理所有类型的异常
    @ExceptionHandler(Exception.class)
    public ResponseMessage handleException(Exception e) {
        log.error("统一异常处理: ", e);

        // 返回统一格式的错误信息
        return new ResponseMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "服务器错误",
                e.getMessage(),
                null
        );
    }

    // 处理数据库约束异常（如外键约束失败等）
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseMessage handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据库约束异常: ", e);

        return new ResponseMessage(
                HttpStatus.BAD_REQUEST.value(),
                "数据库约束错误",
                "违反数据库约束，例如外键约束失败",
                null
        );
    }

    // 处理特定的自定义异常（例如业务逻辑相关的异常）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseMessage handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常: ", e);

        return new ResponseMessage(
                HttpStatus.BAD_REQUEST.value(),
                "非法请求参数",
                e.getMessage(),
                null
        );
    }

    // 处理所有未处理的异常
    @ExceptionHandler(Throwable.class)
    public ResponseMessage handleThrowable(Throwable e) {
        log.error("未知异常: ", e);

        return new ResponseMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "未知错误",
                "系统发生未知错误，请稍后重试。",
                null
        );
    }
}
