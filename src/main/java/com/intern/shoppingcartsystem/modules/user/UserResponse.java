package com.intern.shoppingcartsystem.modules.user;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role,
        boolean isActive,
        boolean emailVerified
) {
}
