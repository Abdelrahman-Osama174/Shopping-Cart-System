package com.intern.shoppingcartsystem.modules.category;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        boolean active,
        Long parentId,
        String parentName
) {
}
