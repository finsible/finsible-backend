package org.finsible.backend.controller;

import jakarta.validation.Valid;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.dto.request.CurrencyRequestDTO;
import org.finsible.backend.dto.response.CurrencyResponseDTO;
import org.finsible.backend.mapper.CurrencyMapper;
import org.finsible.backend.service.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supported-currencies")
public class CurrencyController {
    private final CurrencyService currencyService;
    private final CurrencyMapper currencyMapper;
    public CurrencyController(CurrencyService currencyService, CurrencyMapper currencyMapper) {
        this.currencyService = currencyService;
        this.currencyMapper = currencyMapper;
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<CurrencyResponseDTO>>> getAllCurrencies() {
        List<CurrencyResponseDTO> currencies = currencyService.getAllSupportedCurrencies();
        return ResponseEntity.ok(
                new BaseResponse<>("Supported currencies retrieved successfully", true, currencies)
        );
    }

    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse<CurrencyResponseDTO>> getCurrencyByCode(@PathVariable String code) {
        CurrencyResponseDTO responseDTO = currencyMapper.toCurrencyResponseDTO(currencyService.getCurrencyOrDefault(code));
        return ResponseEntity.ok(new BaseResponse<>("Currency fetched successfully", true, responseDTO));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/")
    public ResponseEntity<BaseResponse<CurrencyResponseDTO>> createSupportedCurrency(@Valid @RequestBody CurrencyRequestDTO currencyRequestDTO) {
        CurrencyResponseDTO responseDTO = currencyService.createSupportedCurrency(currencyRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Successfully added new supported currency", true, responseDTO));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteSupportedCurrency(@PathVariable Long id) {
        currencyService.deleteSupportedCurrency(id);
        return ResponseEntity.ok(new BaseResponse<>("Supported currency removed successfully", true, null));
    }
}
