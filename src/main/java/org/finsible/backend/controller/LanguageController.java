package org.finsible.backend.controller;

import jakarta.validation.Valid;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.dto.request.LanguageRequestDTO;
import org.finsible.backend.dto.response.LanguageResponseDTO;
import org.finsible.backend.mapper.LanguageMapper;
import org.finsible.backend.service.LanguageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supported-languages")
public class LanguageController {
    private final LanguageService languageService;
    private final LanguageMapper languageMapper;

    public LanguageController(LanguageService languageService, LanguageMapper languageMapper) {
        this.languageService = languageService;
        this.languageMapper = languageMapper;
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<LanguageResponseDTO>>> getAllLanguages() {
        List<LanguageResponseDTO> languages = languageService.getAllSupportedLanguages();
        return ResponseEntity.ok(
                new BaseResponse<>("Supported languages retrieved successfully", true, languages)
        );
    }

    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse<LanguageResponseDTO>> getLanguageByCode(@PathVariable String code) {
        LanguageResponseDTO responseDTO = languageMapper.toLanguageResponseDTO(languageService.getLanguageOrDefault(code));
        return ResponseEntity.ok(new BaseResponse<>("Language fetched successfully", true, responseDTO));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/")
    public ResponseEntity<BaseResponse<LanguageResponseDTO>> createSupportedLanguage(@Valid @RequestBody LanguageRequestDTO languageRequestDTO) {
        LanguageResponseDTO responseDTO = languageService.createSupportedLanguage(languageRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Successfully added new supported language", true, responseDTO));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteSupportedLanguage(@PathVariable Long id) {
        languageService.deleteSupportedLanguage(id);
        return ResponseEntity.ok(new BaseResponse<>("Supported language removed successfully", true, null));
    }
}
