package com.tuya.iot.suit.core.exception;


import com.tuya.iot.suit.core.constant.ErrorCode;

/**
 * @author mickey
 * @date 2021年04月20日 21:01
 */
public class ServiceLogicException extends RuntimeException {

    private ErrorCode errorCode;

    public ServiceLogicException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
