package org.finsible.backend.mapper;

import org.finsible.backend.dto.request.CurrencyRequestDTO;
import org.finsible.backend.dto.response.CurrencyResponseDTO;
import org.finsible.backend.entity.SupportedCurrency;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    SupportedCurrency toSupportedCurrency(CurrencyRequestDTO currencyRequestDTO);
    CurrencyResponseDTO toCurrencyResponseDTO(SupportedCurrency currency);
}
