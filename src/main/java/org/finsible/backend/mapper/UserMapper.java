package org.finsible.backend.mapper;

import org.finsible.backend.dto.response.UserResponseDTO;
import org.finsible.backend.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toUserResponseDTO(User user, boolean isNewUser);
}
