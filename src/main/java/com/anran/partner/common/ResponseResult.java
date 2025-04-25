package com.anran.partner.common;

/**
 * 返回结果
 */
public class ResponseResult {
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"success","");
    }

    public static <T> BaseResponse<T> error(ErrorCode error) {
        return new BaseResponse<>(error);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode,String message, String description) {
        return new BaseResponse<>(errorCode, message, description);
    }

    public static <T> BaseResponse<T> error(int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }
}
