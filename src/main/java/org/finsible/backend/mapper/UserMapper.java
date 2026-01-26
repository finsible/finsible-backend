package org.finsible.backend.mapper;

import org.finsible.backend.dto.response.UserResponseDTO;
import org.finsible.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "user.defaultCurrency.code", target = "defaultCurrencyCode")
    @Mapping(source = "user.defaultCurrency.symbol", target = "defaultCurrencySymbol")
    @Mapping(source = "user.defaultLanguage.code", target = "defaultLanguageCode")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "isNewUser", target = "newUser")
    UserResponseDTO toUserResponseDTO(User user, boolean isNewUser);
}
