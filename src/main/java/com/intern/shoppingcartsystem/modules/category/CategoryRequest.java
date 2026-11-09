
package com.intern.shoppingcartsystem.modules.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @Size(max = 2048)
        String imageUrl,

        @Positive
        Long parentId,

        Boolean active
) {
}
