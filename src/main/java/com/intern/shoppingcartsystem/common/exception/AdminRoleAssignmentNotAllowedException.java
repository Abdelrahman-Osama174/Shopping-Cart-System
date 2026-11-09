package com.intern.shoppingcartsystem.common.exception;

public class AdminRoleAssignmentNotAllowedException extends RuntimeException {
    public AdminRoleAssignmentNotAllowedException(String message) {
        super(message);
    }
}
