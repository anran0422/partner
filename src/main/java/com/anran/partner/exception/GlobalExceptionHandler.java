package com.anran.partner.exception;

import com.anran.partner.common.BaseResponse;
import com.anran.partner.common.ErrorCode;
import com.anran.partner.common.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("BusinessException:", e.getMessage(), e);
        return ResponseResult.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e){
        log.error("RuntimeException:", e.getMessage(), e);
        return ResponseResult.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}
