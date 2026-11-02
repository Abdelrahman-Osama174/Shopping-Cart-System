package com.intern.shoppingcartsystem.modules.auth;

import com.intern.shoppingcartsystem.modules.user.User;
import com.intern.shoppingcartsystem.modules.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true, defaultValue = "CUSTOMER")
    @Mapping(target = "isActive", ignore = true, defaultValue = "true")
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(RegisterRequest request);


    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "accessToken", source = "token")
    @Mapping(target = "user", source = "user")
    AuthResponse toResponse(String token, User user);
}
