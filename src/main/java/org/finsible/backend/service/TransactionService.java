package org.finsible.backend.service;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.dto.request.TransactionRequestDTO;
import org.finsible.backend.dto.response.TransactionResponseDTO;
import org.finsible.backend.entity.*;
import org.finsible.backend.mapper.TransactionMapper;
import org.finsible.backend.repository.AccountRepository;
import org.finsible.backend.repository.CategoryRepository;
import org.finsible.backend.repository.SupportedCurrencyRepository;
import org.finsible.backend.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SupportedCurrencyRepository supportedCurrencyRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                            CategoryRepository categoryRepository, TransactionMapper transactionMapper, SupportedCurrencyRepository supportedCurrencyRepository) {
        this.supportedCurrencyRepository = supportedCurrencyRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByType(String userId, Type type) {
        List<Transaction> transactionsByType = transactionRepository.findAllByTypeAndCreatedBy(type, userId);
        return transactionsByType.stream().map(transactionMapper::toTransactionResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByCategory(String userId, Long categoryId) {
        List<Transaction> transactionsByCategory = transactionRepository.findAllByCategory_IdAndCreatedBy(categoryId, userId);
        return transactionsByCategory.stream().map(transactionMapper::toTransactionResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByAccount(String userId, Long accountId) {
        List<Transaction> transactionsByAccount = transactionRepository.findAllByToAccount_IdAndCreatedBy(accountId, userId);
        transactionsByAccount.addAll(transactionRepository.findAllByFromAccount_IdAndCreatedBy(accountId, userId));
        return transactionsByAccount.stream().map(transactionMapper::toTransactionResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByAccountGroup(String userId, Long accountGroupId) {
        List<Transaction> transactionsByAccountGroup = transactionRepository.findAllByToAccount_AccountGroup_IdAndCreatedBy(accountGroupId, userId);
        transactionsByAccountGroup.addAll(transactionRepository.findAllByFromAccount_AccountGroup_IdAndCreatedBy(accountGroupId, userId));
        return transactionsByAccountGroup.stream().map(transactionMapper::toTransactionResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(String userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndCreatedBy(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
        return transactionMapper.toTransactionResponseDTO(transaction);
    }

    @Transactional
    public TransactionResponseDTO createTransaction(String userId, TransactionRequestDTO requestDTO) throws BadRequestException {
        Type type = requestDTO.getType();
        Transaction transaction;

        switch (type) {
            case INCOME -> transaction = createIncomeTransaction(userId, requestDTO);
            case EXPENSE -> transaction = createExpenseTransaction(userId, requestDTO);
            case TRANSFER -> transaction = createTransferTransaction(userId, requestDTO);
            default -> throw new BadRequestException("Invalid transaction type: " + type);
        }

        transactionRepository.save(transaction);
        logger.info("Created {} transaction with id: {}", type, transaction.getId());
        return transactionMapper.toTransactionResponseDTO(transaction);
    }

    private Transaction createIncomeTransaction(String userId, TransactionRequestDTO requestDTO) throws BadRequestException {
        Account toAccount = getAccount(userId, requestDTO.getToAccountId(), "To account");
        Category category = getCategory(userId, requestDTO.getCategoryId(), Type.INCOME);

        Transaction transaction = buildTransaction(requestDTO, toAccount, null, category);
        handleCurrency(toAccount, transaction, requestDTO);
        updateAccountBalance(toAccount, requestDTO.getTotalAmount(), true);

        return transaction;
    }

    private Transaction createExpenseTransaction(String userId, TransactionRequestDTO requestDTO) throws BadRequestException {
        Account fromAccount = getAccount(userId, requestDTO.getFromAccountId(), "From account");
        Category category = getCategory(userId, requestDTO.getCategoryId(), Type.EXPENSE);

        Transaction transaction = buildTransaction(requestDTO, null, fromAccount, category);
        handleCurrency(fromAccount, transaction, requestDTO);
        updateAccountBalance(fromAccount, requestDTO.getTotalAmount(), false);

        return transaction;
    }

    private Transaction createTransferTransaction(String userId, TransactionRequestDTO requestDTO) throws BadRequestException {
        Account toAccount = getAccount(userId, requestDTO.getToAccountId(), "To account");
        Account fromAccount = getAccount(userId, requestDTO.getFromAccountId(), "From account");
        Category category = getCategory(userId, requestDTO.getCategoryId(), Type.TRANSFER);

        Transaction transaction = buildTransaction(requestDTO, toAccount, fromAccount, category);
        handleCurrency(fromAccount, transaction, requestDTO);
        updateAccountBalance(fromAccount, requestDTO.getTotalAmount(), false);
        updateAccountBalance(toAccount, requestDTO.getTotalAmount(), true);

        return transaction;
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(String userId, Long transactionId,
                                                    TransactionRequestDTO requestDTO) throws BadRequestException {
        Transaction transaction = transactionRepository.findByIdAndCreatedBy(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));

        // 1. Validate type cannot change
        if(requestDTO.getType() != null && !requestDTO.getType().equals(transaction.getType())) {
            throw new BadRequestException("Transaction type cannot be updated");
        }

        Type type = transaction.getType();

        // 2. Handle category update
        if(requestDTO.getCategoryId() != null &&
           !requestDTO.getCategoryId().equals(transaction.getCategory().getId())) {
            Category category = getCategory(userId, requestDTO.getCategoryId(), type);
            transaction.setCategory(category);
        }

        // 3. Handle amount change BEFORE account changes
        if(requestDTO.getTotalAmount() != null &&
           !requestDTO.getTotalAmount().equals(transaction.getTotalAmount())) {

            BigDecimal amountDifference = requestDTO.getTotalAmount()
                .subtract(transaction.getTotalAmount());

            handleAmountChange(transaction, amountDifference);
        }

        // 4. Handle toAccount change (INCOME/TRANSFER)
        if(requestDTO.getToAccountId() != null &&
           (type == Type.INCOME || type == Type.TRANSFER) && !requestDTO.getToAccountId().equals(transaction.getToAccount().getId())) {

            updateAccountBalance(transaction.getToAccount(), transaction.getTotalAmount(), false);
            Account newToAccount = getAccount(userId, requestDTO.getToAccountId(), "To account");
            handleCurrency(newToAccount, transaction, requestDTO);
            updateAccountBalance(newToAccount, requestDTO.getTotalAmount(), true);
            transaction.setToAccount(newToAccount);
        }

        // 5. Handle fromAccount change (EXPENSE/TRANSFER)
        if(requestDTO.getFromAccountId() != null &&
           (type == Type.EXPENSE || type == Type.TRANSFER) && !requestDTO.getFromAccountId().equals(transaction.getFromAccount().getId())) {

            updateAccountBalance(transaction.getFromAccount(), transaction.getTotalAmount(), true);

            Account newFromAccount = getAccount(userId, requestDTO.getFromAccountId(), "From account");
            handleCurrency(newFromAccount, transaction, requestDTO);
            updateAccountBalance(newFromAccount, requestDTO.getTotalAmount(), false);
            transaction.setFromAccount(newFromAccount);
        }

        // 6. Update other fields
        transactionMapper.updateTransaction(requestDTO, transaction);

        transactionRepository.save(transaction);
        logger.info("Updated transaction with id: {}", transactionId);
        return transactionMapper.toTransactionResponseDTO(transaction);
    }

    @Transactional
    public void deleteTransaction(String userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndCreatedBy(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));

        BigDecimal totalAmount = transaction.getTotalAmount();
        if(transaction.getType() == Type.INCOME) {
            updateAccountBalance(transaction.getToAccount(), totalAmount, false);
        } else if(transaction.getType() == Type.EXPENSE) {
            updateAccountBalance(transaction.getFromAccount(), totalAmount, true);
        } else if(transaction.getType() == Type.TRANSFER) {
            updateAccountBalance(transaction.getFromAccount(), totalAmount, true);
            updateAccountBalance(transaction.getToAccount(), totalAmount, false);
        }

        transactionRepository.delete(transaction);
        logger.info("Deleted transaction with id: {}", transactionId);
    }

    private void handleAmountChange(Transaction transaction, BigDecimal amountDifference) {
        switch(transaction.getType()) {
            case INCOME -> updateAccountBalance(transaction.getToAccount(), amountDifference, true);
            case EXPENSE -> updateAccountBalance(transaction.getFromAccount(), amountDifference, false);
            case TRANSFER -> {
                updateAccountBalance(transaction.getFromAccount(), amountDifference, false);
                updateAccountBalance(transaction.getToAccount(), amountDifference, true);
            }
        }
    }

    private Account getAccount(String userId, Long accountId, String accountType) throws BadRequestException {
        if (accountId == null) {
            throw new BadRequestException(accountType + " id is null");
        }
        return accountRepository.findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + accountId));
    }

    private Category getCategory(String userId, Long categoryId, Type type) {
        Category category = categoryRepository.findCategoryByIdAndType(categoryId, type);
        if (category == null || (category.getCreatedBy() != null && !Objects.equals(category.getCreatedBy().getId(), userId))) {
            throw new EntityNotFoundException("Category not found with id: " + categoryId + " and type: " + type);
        }
        return category;
    }

    private Transaction buildTransaction(TransactionRequestDTO requestDTO, Account toAccount,
                                       Account fromAccount, Category category) {
        Transaction transaction = transactionMapper.toTransaction(requestDTO);
        transaction.setToAccount(toAccount);
        transaction.setFromAccount(fromAccount);
        transaction.setCategory(category);
        return transaction;
    }

    private void updateAccountBalance(Account account, BigDecimal amount, boolean isCredit) {
        BigDecimal newBalance = isCredit
            ? account.getBalance().add(amount)
            : account.getBalance().subtract(amount);

        account.setBalance(newBalance);
        accountRepository.save(account);
        logger.info("Updated balance in account: {}, New balance: {}", account.getId(), newBalance);
    }

    private void handleCurrency(Account account, Transaction transaction, TransactionRequestDTO requestDTO) {
        if (account.getCurrency() != null && requestDTO.getCurrency() == null) {
            transaction.setCurrency(account.getCurrency());
        }
        if (account.getCurrency() == null){
            throw new EntityNotFoundException("Account currency not set for account id: " + account.getId());
        }
        if (requestDTO.getCurrency() != null) {
            SupportedCurrency currency = supportedCurrencyRepository.findByCode(requestDTO.getCurrency());
            if (currency == null) {
                throw new EntityNotFoundException("Currency not supported: " + requestDTO.getCurrency());
            }
            transaction.setCurrency(currency);
        }
    }
}