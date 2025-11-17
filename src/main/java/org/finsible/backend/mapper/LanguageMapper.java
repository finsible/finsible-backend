package org.finsible.backend.mapper;

import org.finsible.backend.dto.request.LanguageRequestDTO;
import org.finsible.backend.dto.response.LanguageResponseDTO;
import org.finsible.backend.entity.SupportedLanguage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LanguageMapper {
    SupportedLanguage toSupportedLanguage(LanguageRequestDTO languageRequestDTO);
    LanguageResponseDTO toLanguageResponseDTO(SupportedLanguage supportedLanguage);
}
