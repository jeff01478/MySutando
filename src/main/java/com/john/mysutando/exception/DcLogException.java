package com.john.mysutando.exception;

import com.john.mysutando.enums.DcLogErrorCode;

import lombok.Getter;

@Getter
public class DcLogException extends RuntimeException {

    private final DcLogErrorCode errorCode;

    public DcLogException(DcLogErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DcLogException(DcLogErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
