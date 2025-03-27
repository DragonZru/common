package com.ylli.common.exception;

import org.springframework.http.HttpStatus;

public class GenericException extends RuntimeException {

    public int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public GenericException(int code, String message) {
        super(message);
        this.code = code;
    }

    public GenericException(HttpStatus code, String message) {
        super(message);
        this.code = code.value();
    }

    public GenericException(int code) {
        super(HttpStatus.valueOf(code).getReasonPhrase());
        this.code = code;
    }

    /**
     * If true do nothing,  otherwise throws GenericException.
     */
    public static GenericThrowable isTrue(boolean expression) {
        return (httpStatus, message) -> {
            if (!expression) {
                throw new GenericException(httpStatus, message);
            }
        };
    }

    @FunctionalInterface
    public interface GenericThrowable {
        void orElseThrow(HttpStatus httpStatus, String message);
    }
}
