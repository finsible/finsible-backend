package org.finsible.backend.controller;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.dto.request.TransactionRequestDTO;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.dto.response.TransactionResponseDTO;
import org.finsible.backend.entity.Type;
import org.finsible.backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TransactionResponseDTO>> getTransactionById(@RequestAttribute String userId, @PathVariable Long id) {
        TransactionResponseDTO transaction = transactionService.getTransactionById(userId, id);
        return ResponseEntity.ok(new BaseResponse<>("Transaction fetched successfully", true, transaction));
    }

    @GetMapping("/all/{type}")
    public ResponseEntity<BaseResponse<List<TransactionResponseDTO>>> getTransactionsByType(@RequestAttribute String userId, @PathVariable Type type) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByType(userId, type);
        return ResponseEntity.ok(new BaseResponse<>("Transactions of type " + type + " fetched successfully", true, transactions));
    }

    @GetMapping("/all/category/{categoryId}")
    public ResponseEntity<BaseResponse<List<TransactionResponseDTO>>> getTransactionsByCategory(@RequestAttribute String userId, @PathVariable Long categoryId) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByCategory(userId, categoryId);
        return ResponseEntity.ok(new BaseResponse<>("Transactions for category ID " + categoryId + " fetched successfully", true, transactions));
    }

    @GetMapping("/all/account/{accountId}")
    public ResponseEntity<BaseResponse<List<TransactionResponseDTO>>> getTransactionsByAccount(@RequestAttribute String userId, @PathVariable Long accountId) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByAccount(userId, accountId);
        return ResponseEntity.ok(new BaseResponse<>("Transactions for account ID " + accountId + " fetched successfully", true, transactions));
    }

    @GetMapping("/all/account-group/{accountGroupId}")
    public ResponseEntity<BaseResponse<List<TransactionResponseDTO>>> getTransactionsByAccountGroup(@RequestAttribute String userId, @PathVariable Long accountGroupId) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByAccountGroup(userId, accountGroupId);
        return ResponseEntity.ok(new BaseResponse<>("Transactions for account group ID " + accountGroupId + " fetched successfully", true, transactions));
    }

    @PostMapping("/")
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
