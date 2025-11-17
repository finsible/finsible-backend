package org.finsible.backend.controller;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.dto.request.AccountRequestDTO;
import org.finsible.backend.dto.request.CreditCardAccountRequestDTO;
import org.finsible.backend.dto.request.DebitCardAccountRequestDTO;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.dto.response.AccountResponseDTO;
import org.finsible.backend.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private AccountService accountService;

    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{accountGroupId}/")
    public ResponseEntity<BaseResponse<AccountResponseDTO>> createAccount(@RequestAttribute("userId") String userId, @PathVariable Long accountGroupId,
                                                                         @Validated(Create.class) @RequestBody AccountRequestDTO accountRequestDTO) {

        AccountResponseDTO response = accountService.createAccount(userId, accountGroupId, accountRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Account created successfully", true, response));
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<AccountResponseDTO>>> getAccount(@RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(new BaseResponse<>("Accounts fetched successfully", true, accountService.getAccounts(userId)));
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<BaseResponse<AccountResponseDTO>> updateAccount(@RequestAttribute("userId") String userId,
                                                                          @Validated(Update.class) @RequestBody AccountRequestDTO accountRequestDTO,
                                                                          @PathVariable Long accountId) {
        return ResponseEntity.ok(new BaseResponse<>("Account updated successfully", true, accountService.updateAccount(userId, accountId, accountRequestDTO)));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<BaseResponse<String>> deleteAccount(@RequestAttribute("userId") String userId, @PathVariable Long accountId) throws BadRequestException {
        accountService.deleteAccount(userId, accountId);
        return ResponseEntity.ok(new BaseResponse<>("Deleted account", true));
    }

    @PostMapping("/credit-card/")
    public ResponseEntity<BaseResponse<AccountResponseDTO>> createCreditCardAccount(@RequestAttribute("userId") String userId,
                                                                                    @Validated(Create.class) @RequestBody CreditCardAccountRequestDTO creditCardAccountRequestDTO) throws BadRequestException {
        AccountResponseDTO response = accountService.createCreditCardAccount(userId, creditCardAccountRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Credit card account created successfully", true, response));
    }

    @PutMapping("/credit-card/{accountId}")
    public ResponseEntity<BaseResponse<AccountResponseDTO>> updateCreditCardAccount(@RequestAttribute("userId") String userId,
                                                                                    @Validated(Update.class) @RequestBody CreditCardAccountRequestDTO creditCardAccountRequestDTO,
                                                                                    @PathVariable Long accountId) throws BadRequestException {
        AccountResponseDTO response =  accountService.updateCreditCardAccount(userId, accountId, creditCardAccountRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Credit card account updated successfully", true, response));
    }

    @PostMapping("/debit-card/")
    public ResponseEntity<BaseResponse<AccountResponseDTO>> createDebitCardAccount(@RequestAttribute("userId") String userId,
                                                                                  @Validated(Create.class) @RequestBody DebitCardAccountRequestDTO debitCardAccountRequestDTO) throws BadRequestException {
        AccountResponseDTO response = accountService.createDebitCardAccount(userId, debitCardAccountRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Debit card account created successfully", true, response));
    }

    @PutMapping("/debit-card/{accountId}")
    public ResponseEntity<BaseResponse<AccountResponseDTO>> updateDebitCardAccount(@RequestAttribute("userId") String userId,
                                                                                  @Validated(Update.class) @RequestBody DebitCardAccountRequestDTO debitCardAccountRequestDTO,
                                                                                  @PathVariable Long accountId) throws BadRequestException {
        AccountResponseDTO response =  accountService.updateDebitCardAccount(userId, accountId, debitCardAccountRequestDTO);
        return ResponseEntity.ok(new BaseResponse<>("Debit card account updated successfully", true, response));
    }
}
