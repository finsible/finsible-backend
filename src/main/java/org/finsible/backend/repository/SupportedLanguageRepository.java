package org.finsible.backend.repository;

import org.finsible.backend.entity.SupportedLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage, Long> {
    SupportedLanguage findByCode(String code);
}
