package com.intern.shoppingcartsystem.modules.user;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(

        @NotNull
        Boolean active

) {
}