package com.intern.shoppingcartsystem.common.exception;

public class AdminRoleModificationNotAllowedException extends RuntimeException {
    public AdminRoleModificationNotAllowedException(String message) {
        super(message);
    }
}
