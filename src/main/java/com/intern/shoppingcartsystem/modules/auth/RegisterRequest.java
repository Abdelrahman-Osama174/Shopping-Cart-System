package com.intern.shoppingcartsystem.modules.auth;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
        )
        String password,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(?:\\+20|0)?1[0125]\\d{8}$",
                message = "Phone number should be a valid Egyptian mobile number"
        )
        String phone
) {
}
