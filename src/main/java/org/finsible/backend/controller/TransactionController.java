package org.finsible.backend.controller;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.dto.request.TransactionRequestDTO;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.dto.response.DailyTransactionSummaryDTO;
import org.finsible.backend.dto.response.TransactionResponseDTO;
import org.finsible.backend.entity.Type;
import org.finsible.backend.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Page<TransactionResponseDTO>>> getAllTransactions(
            @RequestAttribute String userId,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long accountGroupId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Type type,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Page<TransactionResponseDTO> transactions = transactionService.getAllTransactions(
                userId, startDate, endDate, accountId, accountGroupId, categoryId, type, minAmount, maxAmount, sortBy, search, page, size
        );
        return ResponseEntity.ok(new BaseResponse<>("Transactions fetched successfully", true, transactions));
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<BaseResponse<List<DailyTransactionSummaryDTO>>> getDailyTransactionsSummary(
            @RequestAttribute String userId,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long accountGroupId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Type type
    ) {
        List<DailyTransactionSummaryDTO> summary = transactionService.getDailyTransactionsSummary(
                userId, startDate, endDate, accountId, accountGroupId, categoryId, type
        );
        return ResponseEntity.ok(new BaseResponse<>("Daily transaction summary fetched successfully", true, summary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponseDTO>> getTransactionById(@RequestAttribute String userId, @PathVariable Long id) {
        TransactionResponseDTO transaction = transactionService.getTransactionById(userId, id);
        return ResponseEntity.ok(new BaseResponse<>("Transaction fetched successfully", true, transaction));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<TransactionResponseDTO>> createTransaction(@RequestAttribute String userId,
                                                                                  @Validated(Create.class) @RequestBody TransactionRequestDTO transactionRequestDTO) throws BadRequestException {
        TransactionResponseDTO createdTransaction = transactionService.createTransaction(userId, transactionRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Transaction created successfully", true, createdTransaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponseDTO>> updateTransaction(@RequestAttribute String userId,
                                                                                  @PathVariable Long id,
                                                                                  @Validated(Update.class) @RequestBody TransactionRequestDTO transactionRequestDTO) throws BadRequestException {
        TransactionResponseDTO updatedTransaction = transactionService.updateTransaction(userId, id, transactionRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Transaction updated successfully", true, updatedTransaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteTransaction(@RequestAttribute String userId, @PathVariable Long id) {
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.ok(new BaseResponse<>("Transaction deleted successfully", true));
    }
}
