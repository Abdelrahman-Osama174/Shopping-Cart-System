package com.intern.shoppingcartsystem.modules.auth;

import com.intern.shoppingcartsystem.modules.user.UserResponse;

public record AuthResponse(
        String tokenType,
        String accessToken,
        UserResponse user
) {
}


