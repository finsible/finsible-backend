package org.finsible.backend.repository;

import org.finsible.backend.entity.SupportedCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportedCurrencyRepository extends JpaRepository<SupportedCurrency, Long> {
    SupportedCurrency findByCode(String code);
}
