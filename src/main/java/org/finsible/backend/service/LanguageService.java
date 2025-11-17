package org.finsible.backend.service;

import org.finsible.backend.AppConstants;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.dto.request.LanguageRequestDTO;
import org.finsible.backend.dto.response.LanguageResponseDTO;
import org.finsible.backend.entity.SupportedLanguage;
import org.finsible.backend.mapper.LanguageMapper;
import org.finsible.backend.repository.SupportedLanguageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguageService {
    private final Logger logger = LoggerFactory.getLogger(LanguageService.class);
    private final SupportedLanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    public LanguageService(SupportedLanguageRepository languageRepository, LanguageMapper languageMapper) {
        this.languageMapper = languageMapper;
        this.languageRepository = languageRepository;
    }

    public List<LanguageResponseDTO> getAllSupportedLanguages() {
        List<SupportedLanguage> languages = languageRepository.findAll();
        logger.info("Retrieved {} supported languages", languages.size());

        return languages.stream()
                .map(languageMapper::toLanguageResponseDTO)
                .toList();
    }

    public SupportedLanguage getLanguageOrDefault(String code) {
        SupportedLanguage language = languageRepository.findByCode(code);
        if (language == null) {
            language = languageRepository.findByCode(AppConstants.DEFAULT_LANGUAGE_CODE);
            logger.warn("Requested default language not found. Falling back to {}", language.getName());
        } else {
            logger.info("Default language set to {}", language.getName());
        }
        return language;
    }

    public LanguageResponseDTO createSupportedLanguage(LanguageRequestDTO languageRequestDTO) {
        SupportedLanguage supportedLanguage = languageMapper.toSupportedLanguage(languageRequestDTO);
        // any validation?
        languageRepository.save(supportedLanguage);
        logger.info("Created supported language: {}", supportedLanguage.getName());
        return languageMapper.toLanguageResponseDTO(supportedLanguage);
    }

    public void deleteSupportedLanguage(Long id) {
        SupportedLanguage supportedLanguage = languageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supported language not found"));
        languageRepository.delete(supportedLanguage);
        logger.info("Deleted language with code {}", supportedLanguage.getCode());
    }
}
