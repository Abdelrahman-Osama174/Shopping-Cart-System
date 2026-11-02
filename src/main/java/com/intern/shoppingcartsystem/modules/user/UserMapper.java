package com.intern.shoppingcartsystem.modules.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "isActive", source = "active")
    UserResponse toResponse(User user);
}
