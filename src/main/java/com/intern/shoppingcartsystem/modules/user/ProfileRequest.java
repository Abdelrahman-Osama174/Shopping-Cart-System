package com.intern.shoppingcartsystem.modules.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileRequest(

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(?:\\+20|0)?1[0125]\\d{8}$",
                message = "Phone number should be a valid Egyptian mobile number"
        )
        String phone

) {
}