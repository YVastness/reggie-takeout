package com.yinhaoyu.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author Vastness
 */
@Slf4j
@RestControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {
    public static final String SQL_INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION_MESSAGE = "Duplicate entry";

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException exception) {
        log.info(exception.getMessage());
        if (exception.getMessage().contains(SQL_INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION_MESSAGE)) {
            String[] splits = exception.getMessage().split(" ");
            return Result.error(splits[2] + "已经存在");
        }
        return Result.error("未知错误");
    }

    @ExceptionHandler(CustomException.class)
    public Result<String> exceptionHandler(CustomException exception) {
        return Result.error(exception.getMessage());
    }
}
