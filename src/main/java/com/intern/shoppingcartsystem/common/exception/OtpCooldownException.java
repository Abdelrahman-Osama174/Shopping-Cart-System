package com.intern.shoppingcartsystem.common.exception;

public class OtpCooldownException extends RuntimeException {

    public OtpCooldownException(String message) {
        super(message);
    }
}