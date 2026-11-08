package com.intern.shoppingcartsystem.common.exception;

public class OtpAttemptsExceededException extends RuntimeException {

    public OtpAttemptsExceededException(String message) {
        super(message);
    }
}