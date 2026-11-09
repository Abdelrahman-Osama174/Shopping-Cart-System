package com.intern.shoppingcartsystem.common.exception;

public class AdminStatusModificationNotAllowedException extends RuntimeException {
    public AdminStatusModificationNotAllowedException(String message) {
        super(message);
    }
}
