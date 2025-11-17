package org.finsible.backend.service;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.AppConstants;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.CustomExceptionHandler.UserNotFoundException;
import org.finsible.backend.dto.request.AccountRequestDTO;
import org.finsible.backend.dto.request.CreditCardAccountRequestDTO;
import org.finsible.backend.dto.request.DebitCardAccountRequestDTO;
import org.finsible.backend.dto.response.AccountResponseDTO;
import org.finsible.backend.entity.*;
import org.finsible.backend.mapper.AccountMapper;
import org.finsible.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final SupportedCurrencyRepository currencyRepository;
    private final CreditCardDetailRepository creditCardDetailRepository;
    private final DebitCardDetailsRepository debitCardDetailRepository;
    private final AccountMapper accountMapper;

    public static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, AccountGroupRepository accountGroupRepository,
                          AccountMapper accountMapper, SupportedCurrencyRepository currencyRepository,
                          CreditCardDetailRepository creditCardDetailRepository, DebitCardDetailsRepository debitCardDetailRepository) {
        this.currencyRepository = currencyRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountGroupRepository = accountGroupRepository;
        this.creditCardDetailRepository = creditCardDetailRepository;
        this.debitCardDetailRepository = debitCardDetailRepository;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public AccountResponseDTO createAccount(String userId, Long accountGroupId, AccountRequestDTO accountRequestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: "+userId));

        AccountGroup accountGroup = accountGroupRepository.findAccountGroupById(accountGroupId);
        if(accountGroup == null) throw new EntityNotFoundException("Account group not found");

        SupportedCurrency currency = currencyRepository.findByCode(accountRequestDTO.getCurrencyCode());
        if(currency == null) currency = user.getDefaultCurrency();

        Account account = accountMapper.toAccount(accountRequestDTO);
        if(account.getBalance() == null) account.setBalance(new BigDecimal(0));
        account.setUser(user);
        account.setAccountGroup(accountGroup);
        account.setCurrency(currency);

        accountRepository.save(account);  // If this fails, transaction auto-rolls back

        logger.info("Created account with id {}", account.getId());

        return accountMapper.toAccountResponseDTO(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAccounts(String userId) {
        List<Account> accounts = accountRepository.findAccountsByUser_Id(userId);
        logger.info("Found {} accounts for user {}", accounts.size(), userId);
        return accounts.stream()
                .map(accountMapper::toAccountResponseDTO)
                .toList();
    }

    public void deleteAccount(String userId, Long accountId) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: "+userId));
        Account account = accountRepository.findByIdAndUser(accountId, user);
        if(account == null) throw new EntityNotFoundException("Account not found");
        // todo : Additional checks can be added here (e.g., prevent deletion if account has linked transactions)
        // prevent default account deletion
        if(account.getIsSystemDefault()){
            throw new BadRequestException("Cannot delete system default account");
        }
        accountRepository.deleteById(accountId);
        logger.info("Deleted account with id {}", accountId);
    }

    @Transactional
    public AccountResponseDTO updateAccount(String userId, Long accountId, AccountRequestDTO accountRequestDTO) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found with id: "+userId));
        Account account = accountRepository.findByIdAndUser(accountId, user);
        if(account == null) throw new EntityNotFoundException("Account not found");
        accountMapper.updateAccountFromDto(accountRequestDTO, account);
        if (accountRequestDTO.getCurrencyCode() != null) {
            SupportedCurrency currency = currencyRepository.findByCode(accountRequestDTO.getCurrencyCode());
            if (currency != null) {
                account.setCurrency(currency);
            }
        }
        accountRepository.save(account);
        return accountMapper.toAccountResponseDTO(account);
    }

    @Transactional
    public AccountResponseDTO createCreditCardAccount(String userId, CreditCardAccountRequestDTO creditCardAccountRequestDTO) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        AccountGroup accountGroup = accountGroupRepository.findAccountGroupByName(AppConstants.CREDIT_CARD_ACCOUNT_TYPE);
        if(accountGroup == null) throw new EntityNotFoundException("Credit card account group not found"); // normally should not happen

        SupportedCurrency currency = currencyRepository.findByCode(creditCardAccountRequestDTO.getCurrencyCode());
        if (currency == null) currency = user.getDefaultCurrency();

        if(creditCardAccountRequestDTO.getAvailableCredit() == null){
            creditCardAccountRequestDTO.setAvailableCredit(creditCardAccountRequestDTO.getCreditLimit());
            logger.info("Available credit not provided, setting it to credit limit: {}", creditCardAccountRequestDTO.getCreditLimit());
        }

        if(creditCardAccountRequestDTO.getIsActive() == null){
            creditCardAccountRequestDTO.setIsActive(true);  // default to active
        }
        if(creditCardAccountRequestDTO.getAutoPayEnabled() == null){
            creditCardAccountRequestDTO.setAutoPayEnabled(false); // default auto pay disabled
        }

        Account account = accountMapper.toAccount(creditCardAccountRequestDTO);
//        if (account.getBalance() == null) account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setAccountGroup(accountGroup);
        account.setCurrency(currency);
        account.setBalance(creditCardAccountRequestDTO.getAvailableCredit());

        accountRepository.save(account);
        logger.info("Created credit card account with id {}", account.getId());

        // attach credit card detail
        CreditCardDetail cc = accountMapper.toCreditCardDetail(creditCardAccountRequestDTO);
        cc.setAccount(account);

        if(cc.getBillingDate() == null){
            cc.setBillingDate(1); // default to 1st of month
        }
        if(cc.getDueDate() == null){
            cc.setDueDate(20); // default to 20th of month
        }

        // handle auto-pay account linking
        if(cc.getAutoPayEnabled()){
            Long autoPayFromAccountId = creditCardAccountRequestDTO.getAutoPayFromAccountId();
            if(autoPayFromAccountId != null){
                Account autoPayFromAccount = accountRepository.findByIdAndUser(autoPayFromAccountId, user);
                if(autoPayFromAccount == null){
                    throw new EntityNotFoundException("Auto pay from account not found");
                }
                // auto-pay from account must belong to bank account group
                if(!autoPayFromAccount.getAccountGroup().getName().equals(AppConstants.BANK_ACCOUNT_TYPE)){
                    throw new BadRequestException("Auto pay from account must belong to bank account group");
                }
                cc.setAutoPayFromAccount(autoPayFromAccount);
            } else {
                throw new EntityNotFoundException("Auto pay from account id must be provided when auto pay is enabled");
            }
        }

        creditCardDetailRepository.save(cc);
        logger.info("Created credit-card account details with id {}", account.getId());

        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(account);
        // populate credit card specific fields
        accountMapper.CreditCardAccountResponse(cc, responseDTO);

        return responseDTO;
    }

    @Transactional
    public AccountResponseDTO updateCreditCardAccount(String userId, Long accountId, CreditCardAccountRequestDTO creditCardAccountRequestDTO) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUser(accountId, user);
        if (account == null) throw new EntityNotFoundException("Credit card account not found");
        CreditCardDetail ccDetail = creditCardDetailRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Credit card details not found for account id: " + accountId));

        AccountResponseDTO existingAccountResponseDTO = accountMapper.toAccountResponseDTO(account);
        accountMapper.CreditCardAccountResponse(ccDetail, existingAccountResponseDTO);

        if(creditCardAccountRequestDTO.getAutoPayEnabled() != null && creditCardAccountRequestDTO.getAutoPayEnabled() && creditCardAccountRequestDTO.getAutoPayFromAccountId() != null){
            Account autoPayFromAccount = accountRepository.findByIdAndUser(creditCardAccountRequestDTO.getAutoPayFromAccountId(), user);
            if(autoPayFromAccount == null){
                throw new EntityNotFoundException("Auto pay from account not found");
            }
            // auto-pay from account must belong to bank account group
            if(!autoPayFromAccount.getAccountGroup().getName().equals(AppConstants.BANK_ACCOUNT_TYPE)){
                throw new BadRequestException("Auto pay from account must belong to bank account group");
            }
        }

        accountMapper.updateAccountFromDto(creditCardAccountRequestDTO, account);
        accountMapper.updateCreditCardDetailFromDto(creditCardAccountRequestDTO, ccDetail);

        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(account);
        // populate credit card specific fields
        accountMapper.CreditCardAccountResponse(ccDetail, responseDTO);

        // to avoid unnecessary database writes
        if(existingAccountResponseDTO.equals(responseDTO)){
            return existingAccountResponseDTO;
        }

        accountRepository.save(account);
        logger.info("Updated credit card account with id {}", account.getId());

        // update credit card details
        creditCardDetailRepository.save(ccDetail);
        logger.info("Updated credit card account details with id {}", ccDetail.getAccountId());

        return responseDTO;
    }

    @Transactional
    public AccountResponseDTO createDebitCardAccount(String userId, DebitCardAccountRequestDTO debitCardAccountRequestDTO) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        AccountGroup accountGroup = accountGroupRepository.findAccountGroupByName(AppConstants.DEBIT_CARD_ACCOUNT_TYPE);
        if(accountGroup == null) throw new EntityNotFoundException("Debit card account group not found"); // normally should not happen

        SupportedCurrency currency = currencyRepository.findByCode(debitCardAccountRequestDTO.getCurrencyCode());
        if (currency == null) currency = user.getDefaultCurrency();

        if(debitCardAccountRequestDTO.getIsActive() == null){
            // default to active
            debitCardAccountRequestDTO.setIsActive(true);
        }
        Account debitCardAccount = accountMapper.toAccount(debitCardAccountRequestDTO);
        Account linkedBankAccount = accountRepository.findByIdAndUser(debitCardAccountRequestDTO.getLinkedBankAccountId(), user);
        if(linkedBankAccount == null){
            throw new EntityNotFoundException("Linked bank account not found");
        }
        // linked bank account must belong to bank account group
        if(!linkedBankAccount.getAccountGroup().getName().equals(AppConstants.BANK_ACCOUNT_TYPE)){
            throw new BadRequestException("Linked bank account must belong to bank account group");
        }

        debitCardAccount.setBalance(linkedBankAccount.getBalance());
        debitCardAccount.setUser(user);
        debitCardAccount.setAccountGroup(accountGroup);
        debitCardAccount.setCurrency(currency);

        accountRepository.save(debitCardAccount);
        logger.info("Created debit card account with id {}", debitCardAccount.getId());

        // attach debit card detail
        DebitCardDetail dc = accountMapper.toDebitCardDetail(debitCardAccountRequestDTO);
        dc.setAccount(debitCardAccount);
        dc.setLinkedBankAccount(linkedBankAccount);
        debitCardDetailRepository.save(dc);

        logger.info("Created debit-card account details with id {}", debitCardAccount.getId());

        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(debitCardAccount);
        // populate debit card specific fields
        accountMapper.DebitCardAccountResponse(dc, responseDTO);

        return responseDTO;
    }

    @Transactional
    public AccountResponseDTO updateDebitCardAccount(String userId, Long accountId, DebitCardAccountRequestDTO debitCardAccountRequestDTO)
            throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUser(accountId, user);
        if (account == null) throw new EntityNotFoundException("Debit card account not found");

        DebitCardDetail dcDetail = debitCardDetailRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Debit card details not found for account id: " + accountId));

        AccountResponseDTO existingAccountResponseDTO = accountMapper.toAccountResponseDTO(account);
        accountMapper.DebitCardAccountResponse(dcDetail, existingAccountResponseDTO);

        if (debitCardAccountRequestDTO.getLinkedBankAccountId() != null) {
            Account linkedBankAccount = accountRepository.findByIdAndUser(debitCardAccountRequestDTO.getLinkedBankAccountId(), user);
            if (linkedBankAccount == null) {
                throw new EntityNotFoundException("Linked bank account not found");
            }
            // linked bank account must belong to bank account group
            if (!linkedBankAccount.getAccountGroup().getName().equals(AppConstants.BANK_ACCOUNT_TYPE)) {
                throw new BadRequestException("Linked bank account must belong to bank account group");
            }
            dcDetail.setLinkedBankAccount(linkedBankAccount);
        }

        accountMapper.updateAccountFromDto(debitCardAccountRequestDTO, account);
        accountMapper.updateDebitCardDetailFromDto(debitCardAccountRequestDTO, dcDetail);

        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(account);
        // populate debit card specific fields
        accountMapper.DebitCardAccountResponse(dcDetail, responseDTO);

        // to avoid unnecessary database writes
        if(existingAccountResponseDTO.equals(responseDTO)){
            return existingAccountResponseDTO;
        }

        accountRepository.save(account);
        logger.info("Updated debit card account with id {}", account.getId());

        // update debit card details
        debitCardDetailRepository.save(dcDetail);
        logger.info("Updated debit card account details with id {}", dcDetail.getAccountId());

        return responseDTO;
    }
}
