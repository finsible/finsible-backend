package org.finsible.backend.service;

import org.finsible.backend.AppConstants;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.dto.request.CurrencyRequestDTO;
import org.finsible.backend.dto.response.CurrencyResponseDTO;
import org.finsible.backend.entity.SupportedCurrency;
import org.finsible.backend.mapper.CurrencyMapper;
import org.finsible.backend.repository.SupportedCurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CurrencyService {
    private final SupportedCurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    public CurrencyService(SupportedCurrencyRepository currencyRepository, CurrencyMapper currencyMapper) {
        this.currencyMapper = currencyMapper;
        this.currencyRepository = currencyRepository;
    }

    public List<CurrencyResponseDTO> getAllSupportedCurrencies() {
        List<SupportedCurrency> currencies = currencyRepository.findAll();
        logger.info("Retrieved {} supported currencies", currencies.size());

        return currencies.stream().map(currencyMapper::toCurrencyResponseDTO)
                .toList();
    }

    public SupportedCurrency getCurrencyOrDefault(String code) {
        SupportedCurrency currency = currencyRepository.findByCode(code);
        if (currency == null) {
            currency = currencyRepository.findByCode(AppConstants.DEFAULT_CURRENCY_CODE);
            logger.warn("Requested default currency not found. Falling back to {}", currency.getName());
        } else {
            logger.info("Default currency set to {}", currency.getName());
        }
        return currency;
    }

    public CurrencyResponseDTO createSupportedCurrency(CurrencyRequestDTO currencyDTO) {
        SupportedCurrency currency = currencyMapper.toSupportedCurrency(currencyDTO);
        currencyRepository.save(currency);
        logger.info("Created new supported currency: {}", currency.getName());
        return currencyMapper.toCurrencyResponseDTO(currency);
    }

    public void deleteSupportedCurrency(Long id) {
        SupportedCurrency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supported currency not found"));
        currencyRepository.delete(currency);
        logger.info("Deleted currency with code {}", currency.getCode());
    }
}
