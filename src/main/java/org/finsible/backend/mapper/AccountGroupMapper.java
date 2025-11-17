package org.finsible.backend.mapper;

import org.finsible.backend.dto.request.AccountGroupRequestDTO;
import org.finsible.backend.dto.response.AccountGroupResponseDTO;
import org.finsible.backend.entity.AccountGroup;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AccountGroupMapper {
    AccountGroup toAccountGroup(AccountGroupRequestDTO accountGroupRequestDTO);
    AccountGroupResponseDTO toAccountGroupResponseDTO(AccountGroup accountGroup);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isSystemDefault", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccountGroupFromDto(AccountGroupRequestDTO dto, @MappingTarget AccountGroup accountGroup);
}
