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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final SupportedCurrencyRepository currencyRepository;
    private final CreditCardDetailRepository creditCardDetailRepository;
    private final DebitCardDetailsRepository debitCardDetailRepository;
    private final LoanDetailRepository loanDetailRepository;
    private final AccountMapper accountMapper;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, AccountGroupRepository accountGroupRepository,
                          AccountMapper accountMapper, SupportedCurrencyRepository currencyRepository,
                          CreditCardDetailRepository creditCardDetailRepository, DebitCardDetailsRepository debitCardDetailRepository,
                          LoanDetailRepository loanDetailRepository) {
        this.currencyRepository = currencyRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountGroupRepository = accountGroupRepository;
        this.creditCardDetailRepository = creditCardDetailRepository;
        this.debitCardDetailRepository = debitCardDetailRepository;
        this.loanDetailRepository = loanDetailRepository;
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
        if(account.getBalance() == null) account.setBalance(BigDecimal.ZERO);
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

        Map<String, List<Long>> accountIdsByType = accounts.stream()
                .collect(Collectors.groupingBy(
                        account -> account.getAccountGroup().getName(),  // Group by type
                        Collectors.mapping(Account::getId, Collectors.toList())  // Collect IDs
                ));

        Map<Long, CreditCardDetail> creditCardDetailMap;
        Map<Long, DebitCardDetail> debitCardDetailMap;
        Map<Long, LoanDetail> loanDetailMap;

        if(accountIdsByType.containsKey(AppConstants.CREDIT_CARD_ACCOUNT_TYPE)){
            List<Long> creditCardAccountIds = accountIdsByType.get(AppConstants.CREDIT_CARD_ACCOUNT_TYPE);
            List<CreditCardDetail> creditCardDetails = creditCardDetailRepository.findAllById(creditCardAccountIds);
            creditCardDetailMap = creditCardDetails.stream()
                    .collect(Collectors.toMap(CreditCardDetail::getAccountId, Function.identity()));
        } else {
            creditCardDetailMap = new HashMap<>();
        }
        if(accountIdsByType.containsKey(AppConstants.DEBIT_CARD_ACCOUNT_TYPE)){
            List<Long> debitCardAccountIds = accountIdsByType.get(AppConstants.DEBIT_CARD_ACCOUNT_TYPE);
            List<DebitCardDetail> debitCardDetails = debitCardDetailRepository.findAllById(debitCardAccountIds);
            debitCardDetailMap = debitCardDetails.stream()
                    .collect(Collectors.toMap(DebitCardDetail::getAccountId, Function.identity()));
        } else {
            debitCardDetailMap = new HashMap<>();
        }
        if(accountIdsByType.containsKey(AppConstants.LOAN_ACCOUNT_TYPE)){
            List<Long> loanAccountIds = accountIdsByType.get(AppConstants.LOAN_ACCOUNT_TYPE);
            // Assuming loanDetailRepository is defined and injected
            List<LoanDetail> loanDetails = loanDetailRepository.findAllById(loanAccountIds);
            loanDetailMap = loanDetails.stream()
                    .collect(Collectors.toMap(LoanDetail::getAccountId, Function.identity()));
        } else {
            loanDetailMap = new HashMap<>();
        }

        return accounts.stream()
                .map( account -> mapAccountWithDetails(account, creditCardDetailMap.get(account.getId()),
                        debitCardDetailMap.get(account.getId()),
                        loanDetailMap.get(account.getId())) )
                .toList();
    }

    private AccountResponseDTO mapAccountWithDetails(
            Account account,
            CreditCardDetail creditCardDetail,
            DebitCardDetail debitCardDetail,
            LoanDetail loanDetail) {
        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(account);
        String accountGroupName = account.getAccountGroup().getName();

        if (AppConstants.CREDIT_CARD_ACCOUNT_TYPE.equals(accountGroupName) && creditCardDetail != null) {
            accountMapper.creditCardAccountResponse(creditCardDetail, responseDTO);
        } else if (AppConstants.DEBIT_CARD_ACCOUNT_TYPE.equals(accountGroupName) && debitCardDetail != null) {
            accountMapper.debitCardAccountResponse(debitCardDetail, responseDTO);
        } else if (AppConstants.LOAN_ACCOUNT_TYPE.equals(accountGroupName) && loanDetail != null) {
            // TODO: implement loan type of accounts
            //accountMapper.LoanAccountResponse(loanDetail, responseDTO);
        }
        return responseDTO;
    }

    @Transactional
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
        account.setUser(user);
        account.setAccountGroup(accountGroup);
        account.setCurrency(currency);
        account.setBalance(creditCardAccountRequestDTO.getAvailableCredit());

        accountRepository.save(account);
        logger.info("Created credit card account with id {}", account.getId());

        // attach credit card detail
        CreditCardDetail creditCardDetail = accountMapper.toCreditCardDetail(creditCardAccountRequestDTO);
        creditCardDetail.setAccount(account);

        if(creditCardDetail.getBillingDate() == null){
            creditCardDetail.setBillingDate(1); // default to 1st of month
        }
        if(creditCardDetail.getDueDate() == null){
            creditCardDetail.setDueDate(20); // default to 20th of month
        }

        // handle auto-pay account linking
        if(creditCardAccountRequestDTO.getAutoPayEnabled()){
            Long autoPayFromAccountId = creditCardAccountRequestDTO.getAutoPayFromAccountId();
            Account autoPayFromAccount = findAndValidateBankAccountForUser(autoPayFromAccountId, user, "Auto pay");
            creditCardDetail.setAutoPayFromAccount(autoPayFromAccount);
        }

        creditCardDetailRepository.save(creditCardDetail);
        logger.info("Created credit-card account details with id {}", account.getId());

        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(account);
        // populate credit card specific fields
        accountMapper.creditCardAccountResponse(creditCardDetail, responseDTO);

        return responseDTO;
    }

    @Transactional
    public AccountResponseDTO updateCreditCardAccount(String userId, Long accountId, CreditCardAccountRequestDTO creditCardAccountRequestDTO) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUser(accountId, user);
        if (account == null) throw new EntityNotFoundException("Credit card account not found");
        CreditCardDetail creditCardDetail = creditCardDetailRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Credit card details not found for account id: " + accountId));

        AccountResponseDTO existingAccountResponseDTO = accountMapper.toAccountResponseDTO(account);
        accountMapper.creditCardAccountResponse(creditCardDetail, existingAccountResponseDTO);

        if(creditCardAccountRequestDTO.getAutoPayEnabled() != null){
            if(creditCardAccountRequestDTO.getAutoPayEnabled()){
                Account autoPayFromAccount = findAndValidateBankAccountForUser(creditCardAccountRequestDTO.getAutoPayFromAccountId(), user, "Auto pay");
                creditCardDetail.setAutoPayFromAccount(autoPayFromAccount);
            } else {
                // if disabling auto-pay, clear the linked account
                creditCardDetail.setAutoPayFromAccount(null);
            }
        }

        accountMapper.updateAccountFromDto(creditCardAccountRequestDTO, account);
        accountMapper.updateCreditCardDetailFromDto(creditCardAccountRequestDTO, creditCardDetail);

        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(account);
        // populate credit card specific fields
        accountMapper.creditCardAccountResponse(creditCardDetail, responseDTO);

        // to avoid unnecessary database writes
        if(existingAccountResponseDTO.equals(responseDTO)){
            return existingAccountResponseDTO;
        }

        accountRepository.save(account);
        logger.info("Updated credit card account with id {}", account.getId());

        // update credit card details
        creditCardDetailRepository.save(creditCardDetail);
        logger.info("Updated credit card account details with id {}", creditCardDetail.getAccountId());

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
        Account linkedBankAccount = findAndValidateBankAccountForUser(debitCardAccountRequestDTO.getLinkedBankAccountId(), user, "Linked bank");
        // we should always use the debit card account balance from the linked bank account when needed as storing balance here may lead to inconsistencies

        debitCardAccount.setUser(user);
        debitCardAccount.setAccountGroup(accountGroup);
        debitCardAccount.setCurrency(currency);

        accountRepository.save(debitCardAccount);
        logger.info("Created debit card account with id {}", debitCardAccount.getId());

        // attach debit card detail
        DebitCardDetail debitCardDetail = accountMapper.toDebitCardDetail(debitCardAccountRequestDTO);
        debitCardDetail.setAccount(debitCardAccount);
        debitCardDetail.setLinkedBankAccount(linkedBankAccount);
        debitCardDetailRepository.save(debitCardDetail);

        logger.info("Created debit-card account details with id {}", debitCardAccount.getId());

        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(debitCardAccount);
        // populate debit card specific fields
        accountMapper.debitCardAccountResponse(debitCardDetail, responseDTO);

        return responseDTO;
    }

    @Transactional
    public AccountResponseDTO updateDebitCardAccount(String userId, Long accountId, DebitCardAccountRequestDTO debitCardAccountRequestDTO)
            throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUser(accountId, user);
        if (account == null) throw new EntityNotFoundException("Debit card account not found");

        DebitCardDetail debitCardDetail = debitCardDetailRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Debit card details not found for account id: " + accountId));

        AccountResponseDTO existingAccountResponseDTO = accountMapper.toAccountResponseDTO(account);
        accountMapper.debitCardAccountResponse(debitCardDetail, existingAccountResponseDTO);

        if (debitCardAccountRequestDTO.getLinkedBankAccountId() != null) {
            Account linkedBankAccount = findAndValidateBankAccountForUser(debitCardAccountRequestDTO.getLinkedBankAccountId(), user, "Linked bank");
            debitCardDetail.setLinkedBankAccount(linkedBankAccount);
        }

        accountMapper.updateAccountFromDto(debitCardAccountRequestDTO, account);
        accountMapper.updateDebitCardDetailFromDto(debitCardAccountRequestDTO, debitCardDetail);

        AccountResponseDTO responseDTO = accountMapper.toAccountResponseDTO(account);
        // populate debit card specific fields
        accountMapper.debitCardAccountResponse(debitCardDetail, responseDTO);

        // to avoid unnecessary database writes
        if(existingAccountResponseDTO.equals(responseDTO)){
            return existingAccountResponseDTO;
        }

        accountRepository.save(account);
        logger.info("Updated debit card account with id {}", account.getId());

        // update debit card details
        debitCardDetailRepository.save(debitCardDetail);
        logger.info("Updated debit card account details with id {}", debitCardDetail.getAccountId());

        return responseDTO;
    }

    private Account findAndValidateBankAccountForUser(Long accountId, User user, String field) throws BadRequestException {
        if(accountId == null){
            logger.error("{} account id is null", field);
            throw new BadRequestException(field + " account must be provided");
        }
        Account account = accountRepository.findByIdAndUser(accountId, user);
        if (account == null) {
            logger.error("Account with id {} not found", accountId);
            throw new EntityNotFoundException(field + " account not found");
        }
        if (!account.getAccountGroup().getName().equals(AppConstants.BANK_ACCOUNT_TYPE)) {
            logger.error("{} account must belong to bank account group", field);
            throw new BadRequestException(field + " account must belong to bank account group");
        }
        logger.debug("Found and validated account with id {} for field : {}", accountId, field);
        return account;
    }
}
